package com.baidu.dwpublisher.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-03-08 12:30
 */
@Mapper
public interface OrderMapper {
    //3.获取一天时间内的交易额的总和
    @Select("select sum(total_amount) sum_amount from gmall2020_order_info where create_date = #{date}")
    public int getOrderAmountTotal(String date);

    //4.获取一天时间内的分时的交易额的数值
    @Select("select create_hour, sum(total_amount) sum_amount from gmall2020_order_info where create_date=#{date} group by create_hour")
    public List<Map> getOrderAmountHourMap(String date);
}
