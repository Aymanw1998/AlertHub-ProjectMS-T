package com.mst.client;

import com.mst.model.Loader;
import com.mst.model.Metric;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "metric-service", url="${metric.service.url}")
public interface MetricClient {
    @GetMapping("/get-all")
    public ResponseEntity<List<Metric>> getAllData();
}
