package com.aldi.austin.support.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;


/**
 * @author 3y
 * kafka 发送实现类
 */
@Slf4j
@Service
public class KafkaSendMqServiceImpl implements SendMqService {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Value("${austin.business.tagId.key}")
    private String tagIdKey;

    private static final String TAG_ID = "com.aldi.austin";

    @Override
    public void send(String topic, String jsonValue) {
        List<Header> headers = Collections.singletonList(new RecordHeader(tagIdKey, TAG_ID.getBytes(StandardCharsets.UTF_8)));
        kafkaTemplate.send(new ProducerRecord(topic, null, null, null, jsonValue, headers));
    }


}