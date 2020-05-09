package com.baidu.dwpublisher.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-03-03 20:06
 */
public interface PublisherService {
    // 1.获得实时的日活的全部人数
    public int getDauTotal(String date);

    // 2.获得每小时的日活的人数
    public Map getDauHours(String date);

    //3.得到当日的交易额的数值
    public int getOrderAmountTotal(String date);

    //4.得到当日交易额的分时明确
    public Map getOrderAmountHourMap(String date);

}
