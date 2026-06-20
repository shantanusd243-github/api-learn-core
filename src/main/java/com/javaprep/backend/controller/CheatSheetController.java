package com.javaprep.backend.controller;

import com.javaprep.backend.dto.cheatsheet.CheatSheetItemRequest;
import com.javaprep.backend.dto.cheatsheet.CheatSheetItemResponse;
import com.javaprep.backend.service.CheatSheetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cheatsheet")
@RequiredArgsConstructor
public class CheatSheetController {

    private final CheatSheetService cheatSheetService;

    @GetMapping
    public ResponseEntity<Map<String, List<CheatSheetItemResponse>>> listGrouped() {
        return ResponseEntity.ok(cheatSheetService.listGroupedByCategory());
    }

    @PostMapping
    public ResponseEntity<CheatSheetItemResponse> create(@Valid @RequestBody CheatSheetItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cheatSheetService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CheatSheetItemResponse> update(@PathVariable String id,
                                                           @Valid @RequestBody CheatSheetItemRequest request) {
        return ResponseEntity.ok(cheatSheetService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        cheatSheetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
