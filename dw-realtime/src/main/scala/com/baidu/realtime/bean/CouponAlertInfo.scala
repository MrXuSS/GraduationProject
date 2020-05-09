package com.baidu.realtime.bean

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-03-08 20:04
 */
case class CouponAlertInfo(mid:String,
                           uids:java.util.HashSet[String],
                           itemIds:java.util.HashSet[String],
                           events:java.util.List[String],
                           ts:Long
                          )

