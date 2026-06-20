package com.javaprep.backend.controller;

import com.javaprep.backend.dto.common.TaxonomyItemResponse;
import com.javaprep.backend.dto.tag.TagRequest;
import com.javaprep.backend.service.TaxonomyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TaxonomyService taxonomyService;

    @GetMapping
    public ResponseEntity<List<TaxonomyItemResponse>> list() {
        return ResponseEntity.ok(taxonomyService.listTags());
    }

    @PostMapping
    public ResponseEntity<TaxonomyItemResponse> create(@Valid @RequestBody TagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taxonomyService.createTag(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        taxonomyService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
