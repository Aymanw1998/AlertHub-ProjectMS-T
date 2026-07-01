package com.mst.exceptions;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetricNotFoundException extends Exception{
    private String message;
    public MetricNotFoundException(String message) {
        super(message);
    }
}