package com.devueda.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LibraryEvent {
    private Integer id;
    @NotNull
    @Valid
    private Book book;
    private LibraryEventType libraryEventType;
}
