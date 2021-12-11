package com.devueda.controller;

import com.devueda.model.Book;
import com.devueda.model.LibraryEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(LibraryEventController.class)
class LibraryEventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
    }

    @Test
    void postLibraryEvent() throws Exception {
        LibraryEvent libraryEvent = LibraryEvent
                .builder()
                .id(1)
                .book(Book.builder()
                        .id(1)
                        .name("Lord of The Rings")
                        .author("J. R. R. Tolkien")
                        .build())
                .build();
        mockMvc.perform(post("/v1/libraryevent")
                .content(new ObjectMapper().writeValueAsString(libraryEvent))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.book.name", equalTo("Lord of The Rings")));
    }
}