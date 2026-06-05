package com.mst.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "PlatformInformation")
public class Loader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime timestamp;
    private Long owner_id;
    private String project;
    private String tag;
    @Enumerated(EnumType.STRING)
    private Label label;
    private String developer_id;
    private String task_number;
    @Enumerated(EnumType.STRING)
    private Environment environment;
    private String user_story;
    private Integer task_point;
    private String sprint;
}
