package com.mst.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskAmountResponse {
    private String developerId;
    private Integer sinceDays;
    private Long taskAmount;
}