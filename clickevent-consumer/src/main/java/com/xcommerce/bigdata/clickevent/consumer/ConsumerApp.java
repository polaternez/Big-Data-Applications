package com.xcommerce.bigdata.clickevent.consumer;

import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.Index;


import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

public class ConsumerApp {
    public static void main(String[] args) throws IOException {
        // Create Elasticsearch client
        RestHighLevelClient esClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200)));

        // Create Kafka consumer
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        /*props.put(ConsumerConfig.GROUP_ID_CONFIG, "xcommerce01");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "xcommercecl01");*/
        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(props);
        kafkaConsumer.subscribe(Arrays.asList("clickevent"));

        IndexRequest request = new IndexRequest("clickevent");
        while (true) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ZERO);
            for (ConsumerRecord<String, String> rec : records){
                request.source(rec.value(), XContentType.JSON);
                IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
                System.out.println(response.getId());
            }
        }

    }
}

