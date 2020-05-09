package com.baidu.realtime.bean

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-03-08 19:55
 */
case class EventInfo(
                      mid:String,
                      uid:String,
                      appid:String,
                      area:String,
                      os:String,
                      ch:String,
                      `type`:String,
                      evid:String ,
                      pgid:String ,
                      npgid:String ,
                      itemid:String,
                      var logDate:String,
                      var logHour:String,
                      var ts:Long
                    )
