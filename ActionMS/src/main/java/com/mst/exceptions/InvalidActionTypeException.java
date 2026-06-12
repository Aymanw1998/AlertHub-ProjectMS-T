package com.mst.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidActionTypeException extends Exception {
    private String message;
    public InvalidActionTypeException(String message) {
        super(message);
    }
}
