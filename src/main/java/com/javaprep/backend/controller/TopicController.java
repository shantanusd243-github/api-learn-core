package com.javaprep.backend.controller;

import com.javaprep.backend.dto.common.TaxonomyItemResponse;
import com.javaprep.backend.dto.topic.TopicRequest;
import com.javaprep.backend.service.TaxonomyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TaxonomyService taxonomyService;

    @GetMapping
    public ResponseEntity<List<TaxonomyItemResponse>> list() {
        return ResponseEntity.ok(taxonomyService.listTopics());
    }

    @PostMapping
    public ResponseEntity<TaxonomyItemResponse> create(@Valid @RequestBody TopicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taxonomyService.createTopic(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaxonomyItemResponse> update(@PathVariable String id,
                                                         @Valid @RequestBody TopicRequest request) {
        return ResponseEntity.ok(taxonomyService.updateTopic(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        taxonomyService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }
}
