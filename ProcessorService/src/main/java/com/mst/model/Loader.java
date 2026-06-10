package com.mst.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
public class Loader {
    private Long id;
    private LocalDateTime timestamp;
    private Long owner_id;
    private String project;
    private String tag;
    private Label label;
    private String developer_id;
    private String task_number;
    private Environment environment;
    private String user_story;
    private Integer task_point;
    private String sprint;
}
