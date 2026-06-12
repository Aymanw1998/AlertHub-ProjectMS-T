package com.mst.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidThresholdException extends Exception {
    private String message;
    public InvalidThresholdException(String message) {
        super(message);
    }
}
