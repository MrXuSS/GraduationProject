package com.baidu.realtime.bean

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-02-24 19:39
 */
case class StartUpLog(mid:String,
                      uid:String,
                      appid:String,
                      area:String,
                      os:String,
                      ch:String,
                      logType:String,
                      vs:String,
                      var logDate:String,
                      var logHour:String,
                      var ts:Long
                     )
