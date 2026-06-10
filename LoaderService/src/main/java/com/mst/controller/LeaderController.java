package com.mst.controller;

import com.mst.exceptions.GitHubIntegrationException;
import com.mst.exceptions.LoaderException;
import com.mst.model.Loader;
import com.mst.service.LoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loader")
//localhost:1000/api/
public class LeaderController {
    @Autowired
    private LoaderService service;

    @GetMapping("/all-data")
    public ResponseEntity<List<Loader>> getAllData() {
        return ResponseEntity.ok(service.getAll());
    }
    @GetMapping("/scan")
    //GET http://localhost:1000/api/scan
    public ResponseEntity<String> scan() {
        return ResponseEntity.ok(service.scan());

    }
}
