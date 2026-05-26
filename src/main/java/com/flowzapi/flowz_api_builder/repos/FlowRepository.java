package com.flowzapi.flowz_api_builder.repos;


import com.flowzapi.flowz_api_builder.model.Flow;
import com.flowzapi.flowz_api_builder.model.Step;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowRepository extends MongoRepository<Flow, String> {
    List<Flow> findByProjectId(String projectId);

    void deleteByProjectId(String projectId);
}
