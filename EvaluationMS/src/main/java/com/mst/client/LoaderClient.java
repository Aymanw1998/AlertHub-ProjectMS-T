package com.mst.client;

import com.mst.model.Loader;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "loader-service", url = "${loader.service.url}")
public interface LoaderClient {

    @GetMapping("/get-all")
    ResponseEntity<List<Loader>> getAllData();
}