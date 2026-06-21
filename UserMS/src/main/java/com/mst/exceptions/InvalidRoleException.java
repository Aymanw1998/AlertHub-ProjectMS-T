package com.mst.exceptions;

public class InvalidRoleException extends Exception {
    private String message;
    public InvalidRoleException(String message) {
        super(message);
    }
}
