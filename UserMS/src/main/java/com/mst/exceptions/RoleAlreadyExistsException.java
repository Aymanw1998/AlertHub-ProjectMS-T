package com.mst.exceptions;

public class RoleAlreadyExistsException extends Exception {
    private String message;
    public RoleAlreadyExistsException(String message) {
        super(message);
    }
}
