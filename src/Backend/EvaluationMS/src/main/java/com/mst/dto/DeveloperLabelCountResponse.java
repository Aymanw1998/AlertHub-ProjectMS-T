package com.mst.dto;

import com.mst.model.Label;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperLabelCountResponse {
    private String developerId;
    private Label label;
    private Integer sinceDays;
    private Long taskCount;
}