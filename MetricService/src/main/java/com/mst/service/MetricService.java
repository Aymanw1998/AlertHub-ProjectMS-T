package com.mst.service;

import com.mst.model.Metric;
import com.mst.repo.MetricRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetricService {
    @Autowired
    private MetricRepo repo;

    public List<Metric> getAll(){
        return repo.findAll();
    }

    public Metric getOneById(Long id) throws RuntimeException {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Metric with id " + id + " not found!"));
    }
    public Metric create(Metric info){
        return repo.save(info);
    }
    public Metric update(Long id, Metric info) {
        Metric m = getOneById(id);
        m.setName(info.getName());
        m.setLabel(info.getLabel());
        m.setUser_id(info.getUser_id());
        m.setHours_range(info.getHours_range());
        m.setThreshold(info.getThreshold());
        return repo.save(m);
    }

    public void delete(Long id) {
        getOneById(id);
        repo.deleteById(id);
    }
}
