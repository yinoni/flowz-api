package com.flowzapi.flowz_api_builder.repos;


import com.flowzapi.flowz_api_builder.model.Flow;
import com.flowzapi.flowz_api_builder.model.Step;
import com.flowzapi.flowz_api_builder.repos.projections.FlowOwnerIdProjection;
import com.flowzapi.flowz_api_builder.repos.projections.FlowStepsProjection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlowRepository extends MongoRepository<Flow, String> {
    List<Flow> findByProjectId(String projectId);

    void deleteByProjectId(String projectId);

    Optional<FlowStepsProjection> findStepsProjectedById(String id);

    Optional<FlowOwnerIdProjection> findOwnerIdProjectedById(String id);

    boolean existsByProjectId(String projectId);
}
