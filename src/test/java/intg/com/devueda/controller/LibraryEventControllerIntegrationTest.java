package com.devueda.controller;

import com.devueda.model.Book;
import com.devueda.model.LibraryEventType;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import com.devueda.model.LibraryEvent;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventControllerIntegrationTest {
    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<Integer, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1"
        ,"true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer())
                .createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    @Timeout(5)
    void postLibraryEvent() throws InterruptedException {
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
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent);
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON.toString());

        //when
        ResponseEntity<LibraryEvent> responseEntity = restTemplate.exchange("/v1/libraryevent",
                HttpMethod.POST,
                request,
                LibraryEvent.class);
        //then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        ConsumerRecord<Integer, String> singleRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events");
//        Thread.sleep(3000);
        String value = singleRecord.value();
        assertEquals("{\"id\":null,\"book\":{\"id\":1,\"name\":\"Game Of Thrones\",\"author\":\"Kalisha\"},\"libraryEventType\":\"NEW\"}", value);
    }

    @Test
    @Timeout(5)
    void putLibraryEvent() throws InterruptedException {
        //given
        LibraryEvent libraryEvent
                = LibraryEvent
                .builder()
                .id(1)
                .libraryEventType(LibraryEventType.UPDATE)
                .book(Book
                        .builder()
                        .id(1)
                        .name("Game Of Thrones")
                        .author("Kalisha")
                        .build())
                .build();
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent);
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON.toString());

        //when
        ResponseEntity<LibraryEvent> responseEntity = restTemplate.exchange("/v1/libraryevent",
                HttpMethod.PUT,
                request,
                LibraryEvent.class);
        //then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ConsumerRecord<Integer, String> singleRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events");
//        Thread.sleep(3000);
        String value = singleRecord.value();
        assertEquals("{\"id\":1,\"book\":{\"id\":1,\"name\":\"Game Of Thrones\",\"author\":\"Kalisha\"},\"libraryEventType\":\"UPDATE\"}", value);
    }
}
