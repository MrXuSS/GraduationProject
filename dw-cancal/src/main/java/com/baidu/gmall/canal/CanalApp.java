package com.baidu.gmall.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-03-05 20:28
 */
public class CanalApp {
    public static void main(String[] args){
        // 个人理解,创建一个连接器
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress("192.168.2.201", 11111), "example", "", "");
        while (true){
            canalConnector.connect();
            canalConnector.subscribe("gmall2020.*");

            // 一个message 一次抓取   (一个抓取可以抓多个sql的执行结果集, 100 是指获取 100条sql的运行结果集)
            Message message = canalConnector.get(100);

            if(message.getEntries().size() == 0){
                System.out.println("没有数据,休息五秒!!!!!!!!!!!!!!!!");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                //   忘了看一看 :  https://blog.csdn.net/weixin_40126236/article/details/90407401
                // 一个 entry 代表一个 sql 执行的结果集
                for (CanalEntry.Entry entry : message.getEntries()) {
                    if(entry.getEntryType() == CanalEntry.EntryType.ROWDATA){
                        ByteString storeValue = entry.getStoreValue();
                        CanalEntry.RowChange rowChange = null;
                        try {
                            rowChange = CanalEntry.RowChange.parseFrom(storeValue);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                        List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();
                        String tableName = entry.getHeader().getTableName();

                        // 处理业务数据   发送kafka  到对应的topic中
                        CanalHandler canalHandler = new CanalHandler(rowChange.getEventType(), tableName, rowDatasList);
                        canalHandler.handle();
                    }
                }
            }

        }

    }
}
