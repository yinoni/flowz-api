package com.flowzapi.flowz_api_builder.repos;

import com.flowzapi.flowz_api_builder.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends MongoRepository<Project,String> {

    Optional<Project> findById(String name);

    List<Project> findByUserId(String userId);

}
