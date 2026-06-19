package com.mst.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoggerRequestDTO {
    private String serviceName;
    private String logLevel;
    private String message;
}
