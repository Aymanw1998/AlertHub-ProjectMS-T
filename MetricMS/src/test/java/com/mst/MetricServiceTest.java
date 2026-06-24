package com.mst;

import com.mst.exceptions.InvalidNameException;
import com.mst.exceptions.InvalidThresholdException;
import com.mst.exceptions.MetricNotFoundException;
import com.mst.model.Label;
import com.mst.model.Metric;
import com.mst.repo.MetricRepo;
import com.mst.service.MetricService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = MetricService.class)
class MetricServiceTest {

    @MockitoBean
    private MetricRepo metricRepo;

    @Autowired
    private MetricService metricService;

    @Test
    void getAll_returnsMetricsFromRepository() {
        Metric metric = validMetric();
        when(metricRepo.findAll()).thenReturn(List.of(metric));

        List<Metric> result = metricService.getAll();

        assertEquals(1, result.size());
        assertEquals("Bug counter", result.get(0).getName());
        verify(metricRepo).findAll();
    }

    @Test
    void getOneById_whenMissing_throwsMetricNotFoundException() {
        when(metricRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MetricNotFoundException.class, () -> metricService.getOneById(99L));
    }

    @Test
    void create_whenMetricValid_savesMetric() throws Exception {
        Metric metric = validMetric();
        when(metricRepo.save(metric)).thenReturn(metric);

        Metric saved = metricService.create(metric);

        assertEquals("Bug counter", saved.getName());
        verify(metricRepo).save(metric);
    }

    @Test
    void create_whenNameEmpty_throwsInvalidNameException() {
        Metric metric = validMetric();
        metric.setName(" ");

        assertThrows(InvalidNameException.class, () -> metricService.create(metric));
    }

    @Test
    void create_whenThresholdNegative_throwsInvalidThresholdException() {
        Metric metric = validMetric();
        metric.setThreshold(-1);

        assertThrows(InvalidThresholdException.class, () -> metricService.create(metric));
    }

    @Test
    void update_whenMetricExists_updatesFields() throws Exception {
        Metric existing = validMetric();
        existing.setId(1L);

        Metric update = validMetric();
        update.setName("Documentation counter");
        update.setLabel(Label.DOCUMENTATION);
        update.setThreshold(5);
        update.setTime_frame_hours(12);

        when(metricRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(metricRepo.save(existing)).thenReturn(existing);

        Metric saved = metricService.update(1L, update);

        assertEquals("Documentation counter", saved.getName());
        assertEquals(Label.DOCUMENTATION, saved.getLabel());
        assertEquals(5, saved.getThreshold());
        assertEquals(12, saved.getTime_frame_hours());
        verify(metricRepo).save(existing);
    }

    @Test
    void delete_whenMetricExists_deletesById() throws Exception {
        Metric existing = validMetric();
        existing.setId(1L);
        when(metricRepo.findById(1L)).thenReturn(Optional.of(existing));

        metricService.delete(1L);

        verify(metricRepo).deleteById(1L);
    }

    private Metric validMetric() {
        Metric metric = new Metric();
        metric.setUser_id(1);
        metric.setName("Bug counter");
        metric.setLabel(Label.BUG);
        metric.setThreshold(3);
        metric.setTime_frame_hours(24);
        return metric;
    }
}
