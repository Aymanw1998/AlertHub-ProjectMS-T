package com.mst.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String owner_id;
    private String name;
    private Date create_date;
    @Enumerated(EnumType.STRING)
    private ActionType action_type;
    private LocalTime run_on_time;
    @Enumerated(EnumType.STRING)
    private RunOnDay run_on_day;
    private String message;
    @Column(name = "`to`")
    private String to;
    private Boolean is_enabled;
    private Boolean is_deleted;
    private Timestamp last_update;
    private Timestamp last_run;
    @Column(name = "`condition`")
    private String condition; //"[[1,2],[3]]
}
