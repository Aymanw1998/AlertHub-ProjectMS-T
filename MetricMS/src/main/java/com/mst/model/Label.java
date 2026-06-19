package com.mst.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Label {
    @JsonProperty("bug")
    BUG, //0 //Something is not working

    @JsonProperty("documentation")
    DOCUMENTATION, //Improvements or additions to documentation

    @JsonProperty("enhancement")
    ENHANCEMENT, //New feature or request

    @JsonProperty("help_wanted")
    HELP_WANTED, //Extra attention is needed

    @JsonProperty("duplicate")
    DUPLICATE, //This issue or pull request already exists

    @JsonProperty("invalid")
    INVALID, //This dos not seem right

    @JsonProperty("wontfix")
    WONTFIX, //This will not be worked on

    @JsonProperty("good_first_issue")
    GOOD_FIRST_ISSUE, //Good for newcomers

    @JsonProperty("question")
    QUESTION; //Further information is requested

    public static Label fromString(String value) {
        return Label.valueOf(value.trim().toUpperCase());
    }
}
