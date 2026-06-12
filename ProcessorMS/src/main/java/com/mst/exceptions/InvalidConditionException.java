package com.mst.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidConditionException extends RuntimeException {
    private String message;
    public InvalidConditionException(String message) {
        super(message);
    }
}
