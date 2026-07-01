package com.mst.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ActionType {
    @JsonProperty("sms")
    SMS,
    @JsonProperty("email")
    EMAIL;

    public static ActionType fromString(String value) {
        return ActionType.valueOf(value.trim().toUpperCase());
    }
}
