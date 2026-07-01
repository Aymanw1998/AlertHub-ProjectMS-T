package com.mst.repo;

import com.mst.model.Loader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LoaderRepo extends JpaRepository<Loader, Long> {
    @Query("SELECT MAX(timestamp) FROM Loader")
    LocalDateTime findLastTimestamp();
}
