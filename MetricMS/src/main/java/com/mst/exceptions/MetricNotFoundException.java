package com.mst.exceptions;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Getter
@Setter
public class MetricNotFoundException extends Exception{
    private String message;
    public MetricNotFoundException(String message) {
        super(message);
    }
}