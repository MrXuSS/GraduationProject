package com.baidu.gmall.canal;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-03-06 14:52
 */
public class MyKafkaSender {
    public static KafkaProducer<String,String> kafkaProducer = null;

    public static KafkaProducer<String,String> createKafkaProducer(){
        Properties properties = new Properties();
        properties.put("bootstrap.servers","hadoop201:9092,hadoop202:9092,hadoop203:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String,String> producer = null;
        try {
            producer = new KafkaProducer<String, String>(properties);
        }catch (Exception e){
            e.printStackTrace();
        }
        return producer;
    }

    public static void send(String topic, String msg){
        if(kafkaProducer == null){
            kafkaProducer = createKafkaProducer();
        }
        kafkaProducer.send(new ProducerRecord<String, String>(topic,msg));
    }
}
