package com.mst.controller;

import com.mst.model.Metric;
import com.mst.service.MetricService;
import com.mst.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metric")
public class MetricController {

    @Autowired
    private MetricService service;

    @GetMapping("/get-all")
    public ResponseEntity<List<Metric>> getAllData() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/get-one/{id}")
    public ResponseEntity<?> getOneById(@PathVariable Long id) {
        try {
            Metric metric = service.getOneById(id);
            return ResponseEntity.ok(metric);
        } catch (MetricNotFoundException e) {
            // החזרת קוד 404 יחד עם מלל השגיאה הברור
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Metric m) {
        try {
            Metric createdMetric = service.create(m);
            return ResponseEntity.ok(createdMetric);
        } catch (InvalidNameException | InvalidLabelException |
                 InvalidThresholdException | InvalidTimeFrameException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/create-Many")
    public ResponseEntity<?> create(@RequestBody List<Metric> m) {
            List<Metric> createdMetric = service.createAll(m);
            return ResponseEntity.ok(createdMetric);
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Metric m) {
        try {
            Metric updatedMetric = service.update(id, m);
            return ResponseEntity.ok(updatedMetric);
        } catch (MetricNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidNameException | InvalidLabelException |
                 InvalidThresholdException | InvalidTimeFrameException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok().build();
        } catch (MetricNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}