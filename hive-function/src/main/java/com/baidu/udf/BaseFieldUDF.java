package com.baidu.udf;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-03-28 13:14
 */
public class BaseFieldUDF extends UDF {
    public String evaluate(String line,String key) throws JSONException {
        // 1 处理line  ：   服务器时间 | json
        String[] log = line.split("\\|");

         //2 合法性校验
        if(log.length != 2 || StringUtils.isBlank(log[1])){
            return "";
        }

        // 3 开始处理json
        JSONObject baseObject = new JSONObject(log[1].trim());

        String result = "";

        // 4 根据传进来的key查找相应的value
        if ("et".equals(key)) {
            if (baseObject.has("et")) {
                result = baseObject.getString("et");
            }
        } else if ("st".equals(key)) {
            result = log[0].trim();
        } else {
            JSONObject cm = baseObject.getJSONObject("cm");
            if (cm.has(key)) {
                result = cm.getString(key);
            }
        }
        return result;
    }
}
