package com.devueda.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LibraryEvent {
    private Integer id;
    private Book book;
    private LibraryEventType libraryEventType;
}
