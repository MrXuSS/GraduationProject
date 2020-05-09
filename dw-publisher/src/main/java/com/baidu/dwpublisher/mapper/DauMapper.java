package com.baidu.dwpublisher.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.jasper.compiler.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-03-03 20:07
 */
@Mapper
public interface DauMapper {
    // 1. 获取一天中的全部日活数
    @Select("select count(*) from GMALL2020_DAU where logdate = #{date}")
    public Integer selectDauTotal(String date);

    //2.获取每小时的全部的日活的数量
    @Select("select LOGHOUR LH, count(*) CT from GMALL2020_DAU where logdate = #{date} group by LOGHOUR")
    public List<Map> selectDauHours(String date);
    //  一个list里面存着一行的数据, 以k v 的形式


}
