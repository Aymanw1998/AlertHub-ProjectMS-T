package com.mst.controller;

import com.mst.model.Metric;
import com.mst.service.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<Metric> getOneById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOneById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<Metric> create(@RequestBody Metric m) {
        return ResponseEntity.ok(service.create(m));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Metric> update(@PathVariable Long id, @RequestBody Metric m) {
        return ResponseEntity.ok(service.update(id, m));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

}
