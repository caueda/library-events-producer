package com.devueda.producer;

import com.devueda.model.Book;
import com.devueda.model.LibraryEvent;
import com.devueda.model.LibraryEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryEventProducerUnitTest {

    @InjectMocks
    LibraryEventProducer libraryEventProducer;
    @Mock
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Spy
    ObjectMapper objectMapper;

    @Test
    void sendLibraryEventApproach2_failure() throws JsonProcessingException, ExecutionException, InterruptedException {
        LibraryEvent libraryEvent
                = LibraryEvent
                .builder()
                .id(null)
                .libraryEventType(LibraryEventType.NEW)
                .book(Book
                        .builder()
                        .id(1)
                        .name("Game Of Thrones")
                        .author("Kalisha")
                        .build())
                .build();
        SettableListenableFuture future = new SettableListenableFuture();
        future.setException(new RuntimeException("Exception Calling Kafka"));
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
        assertThrows(Exception.class, () -> libraryEventProducer.sendLibraryEventApproach2(libraryEvent).get());
    }
    @Test
    void sendLibraryEventApproach2_success() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        LibraryEvent libraryEvent
                = LibraryEvent
                .builder()
                .id(null)
                .libraryEventType(LibraryEventType.NEW)
                .book(Book
                        .builder()
                        .id(1)
                        .name("Game Of Thrones")
                        .author("Kalisha")
                        .build())
                .build();
        SettableListenableFuture future = new SettableListenableFuture();
        ProducerRecord<Integer, String> producerRecord =
                new ProducerRecord("library-events", libraryEvent.getId(), objectMapper.writeValueAsString(libraryEvent));
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("library-events", 1), 1, 1, 342, System.currentTimeMillis(), 1, 2);
        SendResult<Integer, String> sendResult = new SendResult<Integer, String>(producerRecord, recordMetadata);
        future.set(sendResult);

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
        //when
        ListenableFuture<SendResult<Integer, String>> listenableFuture = libraryEventProducer.sendLibraryEventApproach2(libraryEvent);
        //then
        SendResult<Integer, String> integerStringSendResult = listenableFuture.get();
        assert integerStringSendResult.getRecordMetadata().partition() == 1;
    }    
}
