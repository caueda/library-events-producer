package com.devueda.producer;

import com.devueda.model.LibraryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class LibraryEventProducer {
    public static final String TOPIC_LIBRARY_EVENTS = "library-events";
    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;
    @Autowired
    ObjectMapper objectMapper;

    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        //This approach will send to the default topic configured in the application.yml
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleError(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message Sent Successfully for the key: {} and the value is {} , partition is {}",
                key,
                value,
                result.getRecordMetadata().partition());
    }

    private void handleError(Integer key, String value, Throwable ex) {
        log.error("Error Sending the Message and the exception is {}", ex.getMessage());
        try {
            throw ex;
        } catch(Throwable throwable) {
            log.error("Error in OnFailure: {}", throwable.getMessage());
        }
    }

    public SendResult<Integer, String> sendLibraryEventSynchronous(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException {
        Integer key = libraryEvent.getId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        SendResult<Integer, String> sendResult = null;
        try {
            //This approach will send to the default topic configured in the application.yml
            sendResult = kafkaTemplate.sendDefault(key, value).get();
        } catch (ExecutionException | InterruptedException ex) {
            log.error("ExecutionException/InterruptedException Sending the Message and the exception is {}", ex.getMessage());
            throw ex;
        } catch (Exception e) {
            log.error("Exception Sending the Message and the exception is {}", e.getMessage());
        }
        return sendResult;
    }

    public void sendLibraryEventApproach2(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        ProducerRecord producerRecord =  buildProducerRecord(key, value, TOPIC_LIBRARY_EVENTS);
        //This approach allows you to send message to any topic, you'll just need to inform it as a parameter
//        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(TOPIC_LIBRARY_EVENTS, key, value);
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleError(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });
    }

    public ProducerRecord buildProducerRecord(Integer key, String value, String topic) {
        List<Header> recordHeaders = List.of(
                new RecordHeader("event-source", "scanner".getBytes())
        );
        return new ProducerRecord(topic, null, key, value, recordHeaders);
    }
}
