package com.javaprep.backend.repository;

import com.javaprep.backend.entity.Company;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CompanyRepository extends MongoRepository<Company, String> {
    Optional<Company> findByName(String name);
    boolean existsByName(String name);
}
