package com.xcommerce.bigdata.clickevent.service;

import com.google.gson.Gson;
import com.xcommerce.bigdata.clickevent.model.ClickRequest;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@Qualifier("click")
public class ProducerServiceImpl implements ProducerService{

    @Autowired
    KafkaMessageProducer kafkaMessageProducer;

    Gson gson;
    JSONObject jsonObject;

    @PostConstruct
    public void init(){
        gson = new Gson();
    }

    @Override
    public ClickRequest producer(ClickRequest request) {
        String jsonData = gson.toJson(request);

        System.out.println(jsonData);
        kafkaMessageProducer.send(jsonData);

        return request;
    }
}
