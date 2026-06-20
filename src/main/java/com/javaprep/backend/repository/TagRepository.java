package com.javaprep.backend.repository;

import com.javaprep.backend.entity.Tag;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TagRepository extends MongoRepository<Tag, String> {
    Optional<Tag> findByName(String name);
    boolean existsByName(String name);
}
