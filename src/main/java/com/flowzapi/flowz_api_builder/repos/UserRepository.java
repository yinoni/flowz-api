package com.flowzapi.flowz_api_builder.repos;

import com.flowzapi.flowz_api_builder.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User,String> {
    User findByEmail(String email);
}
