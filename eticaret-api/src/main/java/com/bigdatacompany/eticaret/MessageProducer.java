package com.bigdatacompany.eticaret;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Component
public class MessageProducer {
    Producer producer;

    @PostConstruct
    public void  init(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, new StringSerializer().getClass().getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, new StringSerializer().getClass().getName());
        producer = new KafkaProducer<String, String>(props);
    }

    public void send(String term){
        ProducerRecord<String, String> rec = new ProducerRecord<>("search-analysis-stream", term);
        producer.send(rec);
    }

    public void close() {
        producer.close();
    }
}
