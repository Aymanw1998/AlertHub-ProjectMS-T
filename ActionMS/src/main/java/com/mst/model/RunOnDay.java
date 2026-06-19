package com.mst.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RunOnDay {
    @JsonProperty("Sunday")
    SUNDAY,
    @JsonProperty("Monday")
    MONDAY,
    @JsonProperty("Tuesday")
    TUESDAY,
    @JsonProperty("Wednesday")
    WEDNESDAY,
    @JsonProperty("Thursday")
    THURSDAY,
    @JsonProperty("Friday")
    FRIDAY,
    @JsonProperty("Saturday")
    SATURDAY,
    @JsonProperty("All")
    ALL;

    public static RunOnDay fromString(String value) {
        return RunOnDay.valueOf(value.trim().toUpperCase());
    }
}
