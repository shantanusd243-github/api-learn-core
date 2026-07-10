package com.javaprep.backend.repository;

import com.javaprep.backend.entity.UserJdQuota;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJdQuotaRepository extends MongoRepository<UserJdQuota, String> {
}