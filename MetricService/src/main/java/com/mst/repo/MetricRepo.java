package com.mst.repo;

import com.mst.model.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricRepo extends JpaRepository<Metric, Long> {
}
