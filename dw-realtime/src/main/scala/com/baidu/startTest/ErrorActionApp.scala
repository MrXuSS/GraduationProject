package com.baidu.startTest

import java.util
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-05-07 14:48
 */
object ErrorActionApp {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("ErrorActionApp")
    val sc = new SparkContext(conf)
    //val ssc = new StreamingContext(sc, Seconds(5))

    //1.读取文件
    val input: RDD[String] = sc.textFile("DeviceData.txt")
    //2.将数据转换   （id，time，data，0,0）
    val mapRDD: RDD[(String, String, String)] = input.map {
      line =>
        val words: Array[String] = line.split(",")
        (words(0), words(1), words(2))
    }
    //
    val filterRDD: RDD[(String, String, String)] = mapRDD.filter(_._3.toInt > 220)

    // 3.对数据进行分组
    val groupByRDD: RDD[(String, Iterable[(String, String, String)])] = filterRDD.groupBy(_._1)

    // 判断数据连续性
    val iterRDD: RDD[Iterable[(String, String, String)]] = groupByRDD.values
    iterRDD.foreach{
      t=>{
        val timeArray = new util.ArrayList[String]()
      }
    }

    val array: Array[Iterable[(String, String, String)]] = iterRDD.collect()
    val array2 = new util.ArrayList[(String, String, String)]()
    for (elem <- array) {
      for (e <- elem) {
        if(array2.isEmpty){
          array2.add(e)
        }else{
          val lastDevice: (String, String, String) = array2.get(array2.size() - 1)
          if(e._2.toInt - lastDevice._2.toInt == 1){
            array2.add(e)
          }else{
            if(array2.size() != 0){

            }
          }
        }
      }
    }
  }
}
