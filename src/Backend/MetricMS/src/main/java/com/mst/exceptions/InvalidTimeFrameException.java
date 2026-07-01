package com.mst.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidTimeFrameException extends Exception {
    private String message;
    public InvalidTimeFrameException(String message) {
        super(message);
    }
}
