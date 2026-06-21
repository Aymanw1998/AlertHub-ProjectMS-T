package com.mst.exceptions;

public class RoleNotFoundException extends Exception {
    private String message;
    public RoleNotFoundException(String message) {
        super(message);
    }
}
