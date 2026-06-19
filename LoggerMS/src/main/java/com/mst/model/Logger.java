package com.mst.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Document(collection = "logs")
@Getter
@Setter
public class Logger {
    @Id
    private String id;
    private LocalDateTime timestamp;
    private String serviceName;
    private String logLevel;
    private String message;
}
