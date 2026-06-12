package com.mst.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidLabelException extends Exception {
    private String message;
    public InvalidLabelException(String message) {
        super(message);
    }
}
