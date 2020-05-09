package com.baidu.flume.interceptor;

import com.baidu.flume.utils.LogUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.Xu
 * @description: ETL拦截器，对数据进行一次清洗
 * @create 2020-03-27 12:31
 */
public class LogETLInterceptor implements Interceptor {
    @Override
    public void initialize() {

    }

    @Override
    public Event intercept(Event event) {
        // 1 获取通知
        byte[] body = event.getBody();
        String log = new String(body, Charset.forName("UTF-8"));

        // 2 判断数据类型并向Header中赋值
        if(log.contains("start")){
            if(LogUtils.validateStart(log)){
                return event;
            }
        }else {
            if(LogUtils.validateEvent(log)){
                return event;
            }
        }
        // 3 返回校验结果
        return null;
    }

    @Override
    public List<Event> intercept(List<Event> events) {
        ArrayList<Event> interceptors = new ArrayList<>();

        for (Event event : events) {
            Event intercept = intercept(event);

            if(intercept != null){
                interceptors.add(intercept);
            }

        }

        return interceptors;
    }

    @Override
    public void close() {

    }

    public static class Bulider implements Interceptor.Builder{

        @Override
        public Interceptor build() {
            return new LogETLInterceptor();
        }

        @Override
        public void configure(Context context) {

        }
    }
}
