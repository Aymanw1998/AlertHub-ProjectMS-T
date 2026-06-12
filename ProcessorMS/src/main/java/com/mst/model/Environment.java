package com.mst.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Environment {
    @JsonProperty("production")
    PRODUCTION,

    @JsonProperty("staging")
    STAGING,

    @JsonProperty("development")
    DEVELOPMENT
}
