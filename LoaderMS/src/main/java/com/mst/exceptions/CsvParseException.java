package com.mst.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CsvParseException extends Exception {
    private String message;

    public CsvParseException(String message) {
        super(message);
    }
}