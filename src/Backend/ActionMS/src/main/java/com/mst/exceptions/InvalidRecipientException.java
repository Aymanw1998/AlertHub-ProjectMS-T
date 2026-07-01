package com.mst.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidRecipientException extends Exception {
    private String message;
    public InvalidRecipientException(String message) {
        super(message);
    }
}
