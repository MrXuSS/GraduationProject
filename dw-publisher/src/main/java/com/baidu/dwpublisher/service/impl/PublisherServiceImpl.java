package com.baidu.dwpublisher.service.impl;

import com.baidu.dwpublisher.mapper.DauMapper;
import com.baidu.dwpublisher.mapper.OrderMapper;
import com.baidu.dwpublisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-03-03 20:06
 */
@Service
public class PublisherServiceImpl implements PublisherService {
    @Autowired
    private  DauMapper dauMapper;
    @Autowired
    private OrderMapper orderMapper;

    @Override
    public int getDauTotal(String date) {
        Integer numTotal = dauMapper.selectDauTotal(date);
        return numTotal;
    }

    @Override
    public Map getDauHours(String date) {
        HashMap dauHourMap = new HashMap();
        List<Map> hourAndCountMapList = dauMapper.selectDauHours(date);

        for (Map map : hourAndCountMapList) {
            dauHourMap.put(map.get("LH"),map.get("CT"));
        }

        return dauHourMap;
    }

    @Override
    public int getOrderAmountTotal(String date) {
        return orderMapper.getOrderAmountTotal(date);
    }

    @Override
    public Map getOrderAmountHourMap(String date) {
        List<Map> orderAmountHourMap = orderMapper.getOrderAmountHourMap(date);
        Map orderAmountHashMap = new HashMap();
        for (Map map : orderAmountHourMap) {
            orderAmountHashMap.put(map.get("CREATE_HOUR"),map.get("SUM_AMOUNT"));
        }
        return orderAmountHashMap;
    }
}
