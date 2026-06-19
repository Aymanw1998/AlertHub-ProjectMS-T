package com.mst.repo;

import com.mst.model.Logger;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoggerRepo extends MongoRepository<Logger, String> {
}
