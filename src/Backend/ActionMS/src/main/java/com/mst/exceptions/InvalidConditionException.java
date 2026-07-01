package com.mst.exceptions;

public class InvalidConditionException extends Exception {
    private String message;
    public InvalidConditionException(String message) {
        super(message);
    }
}
