package com.mst.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidFileNameException extends Exception {
    private String message;

    public InvalidFileNameException(String message) {
        super(message);
    }
}