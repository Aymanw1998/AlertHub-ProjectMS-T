package com.mst.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GitHubIntegrationException extends Exception {
    private String message;

    public GitHubIntegrationException(String message) {
        super(message);
    }
}