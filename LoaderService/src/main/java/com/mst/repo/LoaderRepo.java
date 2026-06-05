package com.mst.repo;

import com.mst.model.Loader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoaderRepo extends JpaRepository<Loader, Long> {
}
