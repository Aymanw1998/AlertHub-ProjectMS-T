package com.mst.service;

import com.mst.model.Metric;
import com.mst.repo.MetricRepo;
import com.mst.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetricService {

    @Autowired
    private MetricRepo repo;

    public List<Metric> getAll() {
        return repo.findAll();
    }

    public Metric getOneById(Long id) throws MetricNotFoundException {
        return repo.findById(id)
                .orElseThrow(() -> new MetricNotFoundException("Metric with id " + id + " not found!"));
    }

    public Metric create(Metric info) throws InvalidNameException, InvalidLabelException,
            InvalidThresholdException, InvalidTimeFrameException {
        validateMetric(info);

        return repo.save(info);
    }

    public Metric update(Long id, Metric info) throws MetricNotFoundException, InvalidNameException,
            InvalidLabelException, InvalidThresholdException,
            InvalidTimeFrameException {

        Metric m = getOneById(id);

        validateMetric(info);

        m.setName(info.getName());
        m.setLabel(info.getLabel());
        m.setUser_id(info.getUser_id());
        m.setTime_frame_hours(info.getTime_frame_hours());
        m.setThreshold(info.getThreshold());

        return repo.save(m);
    }

    public void delete(Long id) throws MetricNotFoundException {
        getOneById(id);
        repo.deleteById(id);
    }

    private void validateMetric(Metric info) throws InvalidNameException, InvalidLabelException,
            InvalidThresholdException, InvalidTimeFrameException {
        // בדיקת תקינות השם שלא יהיה null או ריק
        if (info.getName() == null || info.getName().trim().isEmpty()) {
            throw new InvalidNameException("Metric name cannot be empty");
        }

        // בדיקת תקינות ה-Label (וודאות שלא נשלח null)
        if (info.getLabel() == null) {
            throw new InvalidLabelException("Label is required and must match one of the valid enum values");
        }

        // בדיקת תקינות ה-Threshold (מניעת ערכים שליליים)
        if (info.getThreshold() < 0) {
            throw new InvalidThresholdException("Threshold cannot be negative");
        }

        // בדיקת טווח השעות (בין 1 ל-24)
        if (info.getTime_frame_hours() < 1 || info.getTime_frame_hours() > 24) {
            throw new InvalidTimeFrameException("Hours range must be between 1 and 24");
        }
    }
}