package com.javaprep.backend.controller;

import com.javaprep.backend.entity.UserNote;
import com.javaprep.backend.service.UserNoteService;
import com.javaprep.backend.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class UserNoteController {

    private final UserNoteService userNoteService;

    @GetMapping("/{targetId}")
    public ResponseEntity<UserNote> getNote(@PathVariable String targetId) {
        return userNoteService.getNote(CurrentUser.id(), targetId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{targetId}")
    public ResponseEntity<UserNote> saveOrUpdateNote(
            @PathVariable String targetId,
            @RequestBody UserNote request) {
        return ResponseEntity.ok(userNoteService.saveOrUpdateNote(CurrentUser.id(), targetId, request));
    }

    @DeleteMapping("/{targetId}")
    public ResponseEntity<Void> deleteNote(@PathVariable String targetId) {
        userNoteService.deleteNote(CurrentUser.id(), targetId);
        return ResponseEntity.ok().build();
    }
}