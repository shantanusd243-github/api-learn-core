package com.javaprep.backend.repository;

import com.javaprep.backend.entity.Topic;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends MongoRepository<Topic, String> {
    List<Topic> findAllByOrderByDisplayOrderAsc();
    Optional<Topic> findByName(String name);
    boolean existsByName(String name);
}
