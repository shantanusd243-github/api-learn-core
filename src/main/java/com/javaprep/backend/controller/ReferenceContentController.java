package com.javaprep.backend.controller;

import com.javaprep.backend.dto.reference.ReferenceContentRequest;
import com.javaprep.backend.dto.reference.ReferenceContentResponse;
import com.javaprep.backend.security.CurrentUser;
import com.javaprep.backend.service.ReferenceContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Serves the static reference / strategy pages (Core Java, Java 8, Spring Boot,
 * Microservices, SQL Practice, Cheat Sheet, Senior Gaps, Revision Plan, Story Prep).
 * Reads are public (matches the rest of the content-browsing API); writes are
 * admin-only and let an admin edit a page's HTML body from the admin dashboard.
 */
@RestController
@RequestMapping("/api/reference")
@RequiredArgsConstructor
public class ReferenceContentController {

    private final ReferenceContentService referenceContentService;

    @GetMapping
    public ResponseEntity<List<ReferenceContentResponse>> listAll() {
        return ResponseEntity.ok(referenceContentService.listAll());
    }

    @GetMapping("/{pageKey}")
    public ResponseEntity<ReferenceContentResponse> getByPageKey(@PathVariable String pageKey) {
        return ResponseEntity.ok(referenceContentService.getByPageKey(pageKey));
    }

    @PostMapping
    public ResponseEntity<ReferenceContentResponse> create(@Valid @RequestBody ReferenceContentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(referenceContentService.create(request, CurrentUser.id()));
    }

    @PutMapping("/{pageKey}")
    public ResponseEntity<ReferenceContentResponse> update(@PathVariable String pageKey,
                                                             @Valid @RequestBody ReferenceContentRequest request) {
        return ResponseEntity.ok(referenceContentService.update(pageKey, request, CurrentUser.id()));
    }

    @DeleteMapping("/{pageKey}")
    public ResponseEntity<Void> delete(@PathVariable String pageKey) {
        referenceContentService.delete(pageKey);
        return ResponseEntity.noContent().build();
    }
}
