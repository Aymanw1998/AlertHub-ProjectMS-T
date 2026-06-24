package com.mst.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Label {
    @JsonProperty("bug")
    BUG,

    @JsonProperty("documentation")
    DOCUMENTATION,

    @JsonProperty("enhancement")
    ENHANCEMENT,

    @JsonProperty("help_wanted")
    HELP_WANTED,

    @JsonProperty("duplicate")
    DUPLICATE,

    @JsonProperty("invalid")
    INVALID,

    @JsonProperty("wontfix")
    WONTFIX,

    @JsonProperty("good_first_issue")
    GOOD_FIRST_ISSUE,

    @JsonProperty("question")
    QUESTION;

    public static Label fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Label is required");
        }

        return Label.valueOf(value.trim().toUpperCase());
    }
}