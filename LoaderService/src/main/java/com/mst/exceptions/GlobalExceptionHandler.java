package com.mst.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CsvParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleCsvException(CsvParseException e) {
        return e.getMessage();
    }

    @ExceptionHandler(GitHubIntegrationException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleGitHubException(GitHubIntegrationException e) {
        return e.getMessage();
    }
    @ExceptionHandler(LoaderException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleLoaderException(LoaderException e) {
        return e.getMessage();
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception e) {
        return "Unexpected error: " + e.getMessage();
    }
}