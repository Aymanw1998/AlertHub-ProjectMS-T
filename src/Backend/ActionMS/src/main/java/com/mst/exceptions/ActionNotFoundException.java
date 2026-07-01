package com.mst.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionNotFoundException extends Exception {
    private String message;
    public ActionNotFoundException(String message) {
        super(message);
    }
}
