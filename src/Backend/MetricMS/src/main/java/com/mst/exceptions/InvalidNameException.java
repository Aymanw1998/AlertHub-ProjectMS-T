package com.mst.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidNameException extends Exception{
    private String message;
    public InvalidNameException(String message) {super(message);}
}
