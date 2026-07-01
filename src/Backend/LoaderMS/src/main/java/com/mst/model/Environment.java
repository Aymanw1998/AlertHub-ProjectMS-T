package com.mst.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Environment {
    @JsonProperty("production")
    PRODUCTION,

    @JsonProperty("staging")
    STAGING,

    @JsonProperty("development")
    DEVELOPMENT;

    public static Environment fromString(String value) {
        return Environment.valueOf(value.trim().toUpperCase());
    }
}
