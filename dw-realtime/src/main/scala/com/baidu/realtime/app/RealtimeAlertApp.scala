package com.baidu.realtime.app

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.alibaba.fastjson.JSON
import com.baidu.GmallConstants
import com.baidu.realtime.bean.{CouponAlertInfo, EventInfo}
import com.baidu.realtime.utils.{MyEsUtil, MykafkaUtil}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

// 静态引入break类
import scala.util.control.Breaks._

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-03-08 19:56
 */
object RealtimeAlertApp {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("AlertApp")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(5))

    /*
    	同一设备
    	5分钟内
    	三次不同账号登录
    	领取优惠券
    	没有浏览商品
    	同一设备每分钟只记录一次预警
     */
    // 1. 读取kafka中的数据流
    val inputDStream: InputDStream[ConsumerRecord[String, String]] = MykafkaUtil.getKafkaStream(GmallConstants.KAFKA_TOPIC_EVENT, ssc)

    // 2.将数据进行转换,转换为bean
    val eventInfoDStream: DStream[EventInfo] = inputDStream.map { record =>
      val jsonString: String = record.value()
      val eventInfo: EventInfo = JSON.parseObject(jsonString, classOf[EventInfo])
      val datetime = new Date(eventInfo.ts)
      val formattor = new SimpleDateFormat("yyyy-MM-dd HH")
      val datetimeStr: String = formattor.format(datetime)
      val datetimeArr: Array[String] = datetimeStr.split(" ")
      eventInfo.logDate = datetimeArr(0)
      eventInfo.logHour = datetimeArr(1)
      eventInfo
    }
    // 3. 对数据进行开窗
    //   每 5s 查看前 30s的数据
    val eventInfoWindowDStream: DStream[EventInfo] = eventInfoDStream.window(Seconds(300), Seconds(5))

    // 4.根据设备号进行分组,得到30s中的用户行为信息进行统计
    val groupByMidDStream: DStream[(String, Iterable[EventInfo])] = eventInfoWindowDStream.map(eventInfo => (eventInfo.mid, eventInfo)).groupByKey()

    // 同一个设备
    // 1 三次及以上的领取优惠券 并且 uid 不相同
    //  2 没有浏览商品
    val checkCouponAlertDStream: DStream[(Boolean, CouponAlertInfo)] = groupByMidDStream.map {
      case (mid, eventInfoItr) => {
        val couponUidSet = new util.HashSet[String]() // 记录领取用户的uid
        val itemIdsSet = new util.HashSet[String]() // 记录领取的商品的 id
        val eventIds = new util.ArrayList[String]() // 记录用户行为
        var isClickItem: Boolean = false
        var ifAlert:Boolean = false

        breakable(
          for (elem <- eventInfoItr) {
            eventIds.add(elem.evid)
            if (elem.evid == "coupon") {
              couponUidSet.add(elem.uid)
              itemIdsSet.add(elem.itemid)
            }
            if (elem.evid == "clickItem") {
              isClickItem = true
              break()
            }
          }
        )
          // 形成元组并返回
        if(couponUidSet.size() >= 3 && isClickItem == false){
          ifAlert = true
        }
        (ifAlert,CouponAlertInfo(mid, couponUidSet, itemIdsSet, eventIds, System.currentTimeMillis()))
      }
    }

    //checkCouponAlertDStream.filter(_._1).print()
    checkCouponAlertDStream.print()

    // 对数据进行过滤,   true 的就是警报数据
    val filterDStream: DStream[(Boolean, CouponAlertInfo)] = checkCouponAlertDStream.filter(_._1)

    // 对数据进行转换, 方便最后插入到 ES 中 (给数据添加 id  , 方便去重)

    val alertInfoWithIdDStream: DStream[(String, CouponAlertInfo)] = filterDStream.map {
      case (flag, alertInfo) => {
        val period: Long = alertInfo.ts / 1000L / 60L
        val id: String = alertInfo.mid + "_" + period
        (id, alertInfo)
      }
    }

    // 将数据存储到 ES 中     未完成
    alertInfoWithIdDStream.foreachRDD{
      rdd=>rdd.foreachPartition{
        alertInfoWithIdIter=>{
          MyEsUtil.insertBulk(GmallConstants.ES_INDEX_COUPON_ALERT,alertInfoWithIdIter.toList)
        }
      }
    }

    ssc.start()
    ssc.awaitTermination()

  }

}
