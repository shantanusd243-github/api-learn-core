package com.javaprep.backend.repository;

import com.javaprep.backend.entity.ReferenceContent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReferenceContentRepository extends MongoRepository<ReferenceContent, String> {

    Optional<ReferenceContent> findByPageKey(String pageKey);

    List<ReferenceContent> findAllByOrderByDisplayOrderAsc();

    boolean existsByPageKey(String pageKey);
}
