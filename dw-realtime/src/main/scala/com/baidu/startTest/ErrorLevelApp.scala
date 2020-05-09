//package com.baidu.startTest
//
//import org.apache.spark.rdd.RDD
//import org.apache.spark.streaming.{Seconds, StreamingContext}
//import org.apache.spark.{SparkConf, SparkContext}
//
///**
// * @author Mr.Xu
// * @description:
// * @create 2020-05-07 15:54
// */
//object ErrorLevelApp {
//  def main(args: Array[String]): Unit = {
//    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("ErrorLevelApp")
//    val sc = new SparkContext(conf)
//    //val ssc = new StreamingContext(sc, Seconds(5))
////    1. 异常等级
////    （1）DeviceData > 280，判定为A级异常;
////    （2）260 < DeviceData <= 280，判定为B级异常;
////    （3）220 < DeviceData <= 260，判定为C级异常;
////    （4）否则正常
////    （5）过滤异常数据，丢失数据按照正常计算
////    2. 异常持续时间
////    （1）A级异常持续时间 >= 1小时，则输出异常信息
////    （2）B级异常持续时间 >= 2小时，则输出异常信息
////    （3）C级异常持续时间 >= 3小时，则输出异常信息
////    3. 异常信息结构
////      [UserID,DeviceID,Level,AbnormalStartTime,AbnormalStopTime,AbnormalDurationTime]
////      4. 异常信息输出至HDFS保存
//
//
//    val input: RDD[String] = sc.textFile("DeviceData.txt")
//
////      1. 将异常信息筛选出来
////      2.对异常数据进行区分等级
////      3.按照设备和异常等级进行分组，就可以得到每一个设备各级异常的时间
////      4.筛选出各级异常对应时间的数据
//
//    //  1. 将异常信息筛选出来
//    val inputRDD: RDD[DeviceData] = input.map {
//      line => {
//        val words: Array[String] = line.split(",")
//        DeviceData(words(0), words(1).toLong, words(2).toInt)
//      }
//    }
//    val filterRDD: RDD[DeviceData] = inputRDD.filter(_.deviceData > 220)
//
//    // 2.对异常数据进行分等级
//    val levelRDD: RDD[(String, Long, String)] = filterRDD.map {
//      device => {
//        var level: String = ""
//        if (device.dataTime > 280) {
//          level = "A"
//        } else if (device.deviceData > 260 && device.deviceData <= 280) {
//          level = "B"
//        } else {
//          level = "C"
//        }
//        (device.deviceID, device.dataTime, level)
//      }
//    }
//
//    //3.通过id和level进行分组
//    val groupByRDD: RDD[((String, String), Iterable[(String, Long, String)])] = levelRDD.groupBy(t => (t._1, t._3))
//
//    //4.获取组内的持续时间
//    val durTimeRDD: RDD[(String, String, Long, Long, Long)] = groupByRDD.map {
//      case (t1, t2) => {
//        val sortList: List[(String, Long, String)] = t2.toList.sortBy(_._2)
//        val start: Long = sortList(0)._2
//        val stop: Long = sortList(sortList.length - 1)._2
//        val durTime: Long = stop - start
//        (t1._1, t1._2, start, stop, durTime)
//      }
//    }
//    //5.对时间进行筛选
//    val resultRDD: RDD[(String, String, Long, Long, Long)] = durTimeRDD.filter(
//      t => {
//        (t._2 == "A" && t._5 > 1) || (t._2 == "B" && t._5 > 2) || (t._2 == "C" && t._5 > 3)
//      }
//    )
//    //6.打印最终结果
//    resultRDD.collect().foreach(println)
//
//    //ssc.start()
//    //ssc.awaitTermination()
//  }
//}
