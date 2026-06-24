package com.mst.controller;

import com.mst.dto.DeveloperLabelCountResponse;
import com.mst.dto.LabelAggregateResponse;
import com.mst.dto.TaskAmountResponse;
import com.mst.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService service;

    @GetMapping("/developer/most-label")
    public ResponseEntity<?> getDeveloperWithMostLabel(
            @RequestParam String label,
            @RequestParam Integer since
    ) {
        try {
            DeveloperLabelCountResponse response =
                    service.getDeveloperWithMostLabel(label, since);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
    }

    @GetMapping("/developer/{developerId}/label-aggregate")
    public ResponseEntity<?> getLabelAggregate(
            @PathVariable String developerId,
            @RequestParam Integer since
    ) {
        try {
            LabelAggregateResponse response =
                    service.getLabelAggregate(developerId, since);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
    }

    @GetMapping("/developer/{developerId}/task-amount")
    public ResponseEntity<?> getTaskAmount(
            @PathVariable String developerId,
            @RequestParam Integer since
    ) {
        try {
            TaskAmountResponse response =
                    service.getTaskAmount(developerId, since);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
    }
}