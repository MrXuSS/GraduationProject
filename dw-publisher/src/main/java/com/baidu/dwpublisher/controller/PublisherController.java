package com.baidu.dwpublisher.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.dwpublisher.service.PublisherService;
import io.netty.handler.codec.ProtocolDetectionResult;
import org.apache.commons.lang.time.DateUtils;
import org.apache.phoenix.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-03-03 20:04
 */
@RestController
public class PublisherController {

    @Autowired
    PublisherService publisherService;


    @GetMapping("realtime-total")
    public String realtimeHourDate(String date){
        // 1. 日活总数
        List<Map> list = new ArrayList<>();
        int dauTotal = publisherService.getDauTotal(date);
        Map dauMap = new HashMap<String,Object>();

        dauMap.put("id","dau");
        dauMap.put("name","日活总计");
        dauMap.put("value",dauTotal);

        list.add(dauMap);

        Map orderAmountMap = new HashMap();
        orderAmountMap.put("id","order_amount");
        orderAmountMap.put("name","交易额");
        int orderAmountTotal = publisherService.getOrderAmountTotal(date);
        orderAmountMap.put("value",orderAmountTotal);

        list.add(orderAmountMap);

        return JSON.toJSONString(list);
    }

    @GetMapping("realtime-hour")
    public String realtimeHourDate(String id, String date){
        if("dau".equals(id)){
            Map dauHoursToday = publisherService.getDauHours(date);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("today",dauHoursToday);
            String yesterdayDateString = "";
            try {
                Date dateToday = new SimpleDateFormat("yyyy-MM-dd").parse(date);
                Date dateYesterday = DateUtils.addDays(dateToday, -1);
                yesterdayDateString= new SimpleDateFormat("yyyy-MM-dd").format(dateYesterday);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Map dauHoursYesterday = publisherService.getDauHours(yesterdayDateString);
            jsonObject.put("yesterday",dauHoursYesterday);
            return jsonObject.toJSONString();
        }else if("order_amount".equals(id)){
            Map orderAmountHours = new HashMap();
            Map orderAmountHourMapToday = publisherService.getOrderAmountHourMap(date);
            String yesterday = getYd(date);
            Map orderAmountHourMapYesterday = publisherService.getOrderAmountHourMap(yesterday);
            orderAmountHours.put("yesterday",orderAmountHourMapYesterday);
            orderAmountHours.put("today",orderAmountHourMapToday);

            return JSON.toJSONString(orderAmountHours);
        }
        return null;
    }

    private String  getYd(String td){
        String yd=null;
        SimpleDateFormat formator = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date tdDate = formator.parse(td);
            Date ydDate = DateUtils.addDays(tdDate, -1);
            yd=formator.format(ydDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return yd;
    }

}
