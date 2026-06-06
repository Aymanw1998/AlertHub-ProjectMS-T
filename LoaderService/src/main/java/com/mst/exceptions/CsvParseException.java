package com.mst.exceptions;

public class CsvParseException extends RuntimeException{
    public CsvParseException(String message){
        super(message);
    }
}
