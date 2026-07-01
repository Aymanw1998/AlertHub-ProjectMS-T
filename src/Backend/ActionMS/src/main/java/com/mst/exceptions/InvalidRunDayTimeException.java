package com.mst.exceptions;

public class InvalidRunDayTimeException extends Exception {
    private String message;
    public InvalidRunDayTimeException(String message) {
        super(message);
    }
}
