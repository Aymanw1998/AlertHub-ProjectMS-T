package com.mst.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalServiceException extends RuntimeException {
    private String message;
    public ExternalServiceException(String message) {
        super(message);
    }
}
