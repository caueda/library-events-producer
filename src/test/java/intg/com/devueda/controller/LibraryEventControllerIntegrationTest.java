package com.devueda.controller;

import com.devueda.model.Book;
import com.devueda.model.LibraryEventType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import com.devueda.model.LibraryEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LibraryEventControllerIntegrationTest {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void postLibraryEvent() {
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
    }
}
