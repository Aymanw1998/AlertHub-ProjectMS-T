package com.mst.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidMessageException extends Exception {
    private String message;
    public InvalidMessageException(String message) {
        super(message);
    }
}
