package com.baidu.realtime.app

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.alibaba.fastjson.{JSON, JSONObject}
import com.baidu.GmallConstants
import com.baidu.realtime.bean.StartUpLog
import com.baidu.realtime.utils.{MykafkaUtil, RedisUtil}
import org.apache.hadoop.conf.Configuration
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import redis.clients.jedis.Jedis

// 引入静态转换的包
import org.apache.phoenix.spark._

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-02-24 19:44
 */
object RealtimeStartUpLogApp {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local[1]").setAppName("gmall2020")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(5))
    //1.消费kafka
    val startuplogDStream: InputDStream[ConsumerRecord[String, String]] = MykafkaUtil.getKafkaStream(GmallConstants.KAFKA_TOPIC_STARTUP, ssc)

    //2. 数据流转换为case class 对象,  补充两个时间字段
    val startupObjectDStream: DStream[StartUpLog] = startuplogDStream.map {
      record => {
        val jsonString: String = record.value()
        val startuplog: StartUpLog = JSON.parseObject(jsonString, classOf[StartUpLog])
        val dataTimeStr: String = new SimpleDateFormat("yyyy-MM-dd HH").format(new Date(startuplog.ts))
        val dateArr: Array[String] = dataTimeStr.split(" ")
        startuplog.logDate = dateArr(0)
        startuplog.logHour = dateArr(1)
        startuplog
      }
    }


    //3. 利用用户清单进行过滤 去重  只保留清单中不存在的用户访问记录
    val filterDStream: DStream[StartUpLog] = startupObjectDStream.transform {
      rdd => {
        val jedis: Jedis = RedisUtil.getJedisClient
        val dateStr: String = new SimpleDateFormat("yyyy-MM-dd").format(new Date())

        val key: String = "dau:" + dateStr
        val dauMidSet: util.Set[String] = jedis.smembers(key)
        jedis.close()

        val dauMidBC: Broadcast[util.Set[String]] = ssc.sparkContext.broadcast(dauMidSet)
        println("过滤前:" + rdd.count())
        val filteredRDD = rdd.filter { startuplog =>
          val dauMidSet: util.Set[String] = dauMidBC.value
          !dauMidSet.contains(startuplog.mid)
        }
        println("过滤后:" + filteredRDD.count())
        filteredRDD
      }
    }

    //4.批次内去重: 按照mid分组,每组去第一个值
    val groupByMidDStream: DStream[(String, Iterable[StartUpLog])] = filterDStream.map(startuplog => (startuplog.mid, startuplog)).groupByKey()
    val faltMapDStream: DStream[StartUpLog] = groupByMidDStream.flatMap {
      case (mid, startuplogItr) => {
        startuplogItr.toList.take(1)
      }
    }

    //5. 保存今日访问的用户   dau:2019-xx-xx  value : mid
    faltMapDStream.foreachRDD{
      rdd=>{
        rdd.foreachPartition{
          startuplogItr=>{
            val jedisClient: Jedis = RedisUtil.getJedisClient
            for (startuplog <- startuplogItr) {
              val key: String = "dau:" + startuplog.logDate
              jedisClient.sadd(key,startuplog.mid)
              println(startuplog)
            }
            jedisClient.close()
          }
        }
      }
    }

    //6.将数据写入到hbase,  通过phoenix
    faltMapDStream.foreachRDD{
      rdd=>rdd.saveToPhoenix("GMALL2020_DAU",Seq("MID","UID","APPID","AREA","OS","CH","TYPE","VS","LOGDATE","LOGHOUR","TS"),new Configuration, Some("hadoop201,hadoop202,hadoop203:2181"))
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
