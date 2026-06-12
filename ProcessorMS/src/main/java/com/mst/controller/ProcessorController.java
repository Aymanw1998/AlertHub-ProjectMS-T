package com.mst.controller;

import com.mst.client.LoaderClient;
import com.mst.model.Loader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/processor")
public class ProcessorController {
    @Autowired
    private LoaderClient client;

    @GetMapping("/get-all-data-loader")
    public ResponseEntity<List<Loader>> getAllDataFromLoader() {
        try {
            ResponseEntity<List<Loader>> res = client.getAllData();
            return ResponseEntity.ok(res.getBody());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
