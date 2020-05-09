package com.baidu.gmall.canal;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.baidu.GmallConstants;

import java.util.List;

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-03-06 14:45
 */
public class CanalHandler {

    CanalEntry.EventType eventType;

    String tableName;

    List<CanalEntry.RowData> rowDataList;

    public CanalHandler(CanalEntry.EventType eventType, String tableName, List<CanalEntry.RowData> rowDataList){
        this.eventType = eventType;
        this.rowDataList = rowDataList;
        this.tableName = tableName;
    }

    public void handle(){
        if("order_info".equals(tableName) && CanalEntry.EventType.INSERT == eventType){
            rowDateListToKafka(GmallConstants.KAFKA_TOPIC_ORDER_DETAIL);
        }else if("user_info".equals(tableName) && (CanalEntry.EventType.INSERT == eventType || CanalEntry.EventType.UPDATE ==eventType)){
            rowDateListToKafka(GmallConstants.KAFKA_TOPIC_USER_DETAIL);
        }
    }

    private void rowDateListToKafka(String kafkaTopic){
        for (CanalEntry.RowData rowData : rowDataList) {
            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
            JSONObject jsonObject = new JSONObject();
            for (CanalEntry.Column column : afterColumnsList) {
                System.out.println(column.getName()+":::"+column.getValue());
                jsonObject.put(column.getName(),column.getValue());
            }
            MyKafkaSender.send(kafkaTopic,jsonObject.toJSONString());
        }
    }

}
