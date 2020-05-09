package com.baidu.realtime.app

import com.alibaba.fastjson.JSON
import com.baidu.GmallConstants
import com.baidu.realtime.bean.OrderInfo
import com.baidu.realtime.utils.MykafkaUtil
import org.apache.hadoop.conf.Configuration
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

// 引入静态转换的包
import org.apache.phoenix.spark._

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-03-07 19:38
 */
object RealtimeOrderApp {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("gmall2020_order")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(5))
    // 1.获取kafka中的数据流
    val orderInfoStream: InputDStream[ConsumerRecord[String, String]] = MykafkaUtil.getKafkaStream(GmallConstants.KAFKA_TOPIC_ORDER_DETAIL, ssc)

    //2.对数据流进行装换, 转换为 bean
    val orderDStream: DStream[OrderInfo] = orderInfoStream.map(_.value()).map {
      orderJson => {
        val orderInfo: OrderInfo = JSON.parseObject(orderJson, classOf[OrderInfo])
        val create_dateArrays: Array[String] = orderInfo.create_time.split(" ")
        orderInfo.create_date = create_dateArrays(0)
        orderInfo.create_hour= create_dateArrays(1).split(":")(0)
        orderInfo.consignee_tel = "*******" + orderInfo.consignee_tel.splitAt(7)._2
        orderInfo
      }
    }

    //3.将数据存储到Hbase
    orderDStream.foreachRDD(rdd=>{
      val configuration = new Configuration()
      println(rdd.collect().mkString("\n"))
      rdd.saveToPhoenix("GMALL2020_ORDER_INFO",
        Seq("ID",
      "PROVINCE_ID",
      "CONSIGNEE",
      "ORDER_COMMENT",
      "CONSIGNEE_TEL",
      "ORDER_STATUS",
      "PAYMENT_WAY",
      "USER_ID",
      "IMG_URL",
      "TOTAL_AMOUNT",
      "EXPIRE_TIME",
      "DELIVERY_ADDRESS",
      "CREATE_TIME",
      "OPERATE_TIME",
      "TRACKING_NO",
      "PARENT_ORDER_ID",
      "OUT_TRADE_NO",
      "TRADE_BODY",
      "CREATE_DATE",
      "CREATE_HOUR"),
        configuration,
        Some("hadoop201,hadoop202,hadoop203:2181"))
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
