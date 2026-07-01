package com.mst.exceptions;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MetricNotFoundException extends RuntimeException {
    private String message;
    public MetricNotFoundException(String message) {
        super(message);
    }
}
