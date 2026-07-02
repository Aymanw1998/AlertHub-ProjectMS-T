package com.mst.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.Date;

@Getter
@Setter
public class Action {
    private Long id;
    private String owner_id;
    private String name;
    private ActionType action_type;
    private String message;
    private String to;
    private String condition; //"[[1,2],[3]]
}
