package com.mst.controller;

import com.mst.exceptions.GitHubIntegrationException;
import com.mst.exceptions.LoaderException;
import com.mst.service.LoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LeaderController {
    @Autowired
    private LoaderService service;

    @GetMapping("/scan")
    public ResponseEntity<String> scan() {
        return ResponseEntity.ok(service.scan());

    }
}
