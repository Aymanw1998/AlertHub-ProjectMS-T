package com.mst.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Notification {
    private Long owner_id;
    private String name;
    private String message;
    private String to;
}
