package com.mst.exceptions;

public class InvalidUserException extends Exception {
    private String message;
    public InvalidUserException(String message) {
        super(message);
    }
}
