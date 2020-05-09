package com.baidu.dwlogger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.GmallConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.sound.midi.Soundbank;


/**
 * @author Mr.Xu
 * @description:
 * @create 2020-02-23 16:17
 */


@RestController
@Slf4j
public class LogJsonController {
    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @PostMapping("log")
    public String doLog(String logString){

        // 0 补充时间戳
        JSONObject jsonObject = JSON.parseObject(logString);
        jsonObject.put("ts",System.currentTimeMillis());

        // 1 落盘到文件
        String jsonString = jsonObject.toJSONString();
        // 使用 log 落盘需要安装 lombok 插件 // 注意移动网络下无法下载插件
        log.info(jsonString);

        System.out.println(jsonString);

        if("startup".equals(jsonObject.getString("type"))){
            kafkaTemplate.send(GmallConstants.KAFKA_TOPIC_STARTUP,jsonString);
        }else {
            kafkaTemplate.send(GmallConstants.KAFKA_TOPIC_EVENT,jsonString);
        }
        return "success";
    }
}
