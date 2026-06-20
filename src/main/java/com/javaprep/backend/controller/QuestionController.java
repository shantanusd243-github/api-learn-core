package com.javaprep.backend.controller;

import com.javaprep.backend.dto.common.PageResponse;
import com.javaprep.backend.dto.question.QuestionRequest;
import com.javaprep.backend.dto.question.QuestionResponse;
import com.javaprep.backend.entity.QuestionType;
import com.javaprep.backend.security.CurrentUser;
import com.javaprep.backend.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<PageResponse<QuestionResponse>> search(
            @RequestParam(required = false) QuestionType type,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String week, // <--- ADDED THIS
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable;
        if (QuestionType.THEORY.equals(type)) {
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "topic"));
        } else {
            pageable = PageRequest.of(page, size);
        }

        // <--- ADDED 'week' TO THE SERVICE CALL BELOW
        var result = questionService.search(type, topic, category, difficulty, priority, tag, company, search, week, pageable, currentUserIdOrNull());

        return ResponseEntity.ok(PageResponse.from(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(questionService.getById(id, currentUserIdOrNull()));
    }

    @GetMapping("/tags/{tag}")
    public ResponseEntity<List<QuestionResponse>> byTag(@PathVariable String tag) {
        return ResponseEntity.ok(questionService.findByTag(tag));
    }

    @GetMapping("/topics/{topic}")
    public ResponseEntity<List<QuestionResponse>> byTopic(@PathVariable String topic) {
        return ResponseEntity.ok(questionService.findByTopic(topic));
    }

    @GetMapping("/companies/{company}")
    public ResponseEntity<List<QuestionResponse>> byCompany(@PathVariable String company) {
        return ResponseEntity.ok(questionService.findByCompany(company));
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> create(@Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.create(request, CurrentUser.id()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponse> update(@PathVariable String id,
                                                     @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.update(id, request, CurrentUser.id()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        questionService.delete(id, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    private String currentUserIdOrNull() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        try {
            return CurrentUser.id();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, List<String>>> getMetadata() {
        return ResponseEntity.ok(questionService.getFilterMetadata());
    }

    @GetMapping("/topics")
    public ResponseEntity<List<String>> getAllTopics() {
        return ResponseEntity.ok(questionService.getAllTopics());
    }
}
