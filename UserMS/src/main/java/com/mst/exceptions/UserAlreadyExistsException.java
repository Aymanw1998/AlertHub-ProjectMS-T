package com.mst.exceptions;

public class UserAlreadyExistsException extends Exception {
    private String message;
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
