package com.devueda.controller;

import com.devueda.model.Book;
import com.devueda.model.LibraryEvent;
import com.devueda.model.LibraryEventType;
import com.devueda.producer.LibraryEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(LibraryEventController.class)
@AutoConfigureMockMvc
public class LibraryEventControllerUnitTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    LibraryEventProducer libraryEventProducer;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void postLibraryEvent() throws Exception {
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
        //        doNothing().when(libraryEventProducer).sendLibraryEventApproach2(isA(LibraryEvent.class));
        when(libraryEventProducer.sendLibraryEventApproach2(isA(LibraryEvent.class))).thenReturn(null);
        mockMvc.perform(post("/v1/libraryevent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(libraryEvent))
        ).andExpect(status().isCreated());
    }

    @Test
    void postLibraryEvent_4xx() throws Exception {
        LibraryEvent libraryEvent
                = LibraryEvent
                .builder()
                .id(null)
                .libraryEventType(LibraryEventType.NEW)
                .book(Book
                        .builder()
                        .id(null)
                        .name(null)
                        .author("Kalisha")
                        .build())
                .build();
//        doNothing().when(libraryEventProducer).sendLibraryEventApproach2(isA(LibraryEvent.class));
        when(libraryEventProducer.sendLibraryEventApproach2(isA(LibraryEvent.class))).thenReturn(null);


        String expectedErrorMessage = "book.id - must not be null, book.name - must not be blank";
        mockMvc.perform(post("/v1/libraryevent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(libraryEvent))
                ).andExpect(status().is4xxClientError())
                .andExpect(content().string(expectedErrorMessage));
    }
}
