package com.mst.controller;

import com.mst.model.Logger;
import com.mst.service.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logger")
public class LoggerController {
    @Autowired
    private LoggerService service;

    @GetMapping("/get-all")
    public ResponseEntity<List<Logger>> getAll() {
        return ResponseEntity.ok(service.getAllData());
    }
    @PostMapping("/create")
    public ResponseEntity<Logger> create(@RequestBody Logger info){
        return ResponseEntity.ok(service.create(info));
    }


}
