package com.mst.service;

import com.mst.model.Logger;
import com.mst.repo.LoggerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoggerService {
    @Autowired
    private LoggerRepo repo;

    public List<Logger> getAllData() {
        return repo.findAll();
    }

    public Logger create(Logger info) {
        info.setTimestamp(LocalDateTime.now());
        return repo.save(info);
    }
}
