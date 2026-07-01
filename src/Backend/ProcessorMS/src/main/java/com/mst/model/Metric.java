package com.mst.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Setter
@Getter
public class Metric {

    private Long id;
    private int user_id;
    private String name;
    private Label label;
    private int threshold;
    @Min(value = 1, message = "Hours range must be at least 1")
    @Max(value = 24, message = "Hours range cannot exceed 24")
    private int  time_frame_hours;
}
