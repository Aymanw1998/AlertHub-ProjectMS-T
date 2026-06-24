package com.mst.dto;

import com.mst.model.Label;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LabelAggregateResponse {
    private String developerId;
    private Integer sinceDays;
    private Map<Label, Long> labelCounts;
}