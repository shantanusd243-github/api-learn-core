package com.javaprep.backend.controller;

import com.javaprep.backend.entity.TextHighlight;
import com.javaprep.backend.service.HighlightService;
import com.javaprep.backend.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/highlights")
@RequiredArgsConstructor
public class HighlightController {

    private final HighlightService highlightService;

    @PostMapping
    public ResponseEntity<TextHighlight> saveHighlight(@RequestBody TextHighlight request) {
        return ResponseEntity.ok(highlightService.saveHighlight(CurrentUser.id(), request));
    }

    @GetMapping("/{targetId}")
    public ResponseEntity<List<TextHighlight>> getHighlights(@PathVariable String targetId) {
        return ResponseEntity.ok(highlightService.getHighlights(CurrentUser.id(), targetId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHighlight(@PathVariable String id) {
        highlightService.deleteHighlight(id);
        return ResponseEntity.ok().build();
    }
}