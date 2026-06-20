package com.javaprep.backend.controller;

import com.javaprep.backend.dto.bookmark.BookmarkResponse;
import com.javaprep.backend.security.CurrentUser;
import com.javaprep.backend.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping
    public ResponseEntity<List<BookmarkResponse>> list() {
        return ResponseEntity.ok(bookmarkService.listBookmarks(CurrentUser.id()));
    }

    @PostMapping("/{questionId}")
    public ResponseEntity<BookmarkResponse> add(@PathVariable String questionId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookmarkService.addBookmark(CurrentUser.id(), questionId));
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> remove(@PathVariable String questionId) {
        bookmarkService.removeBookmark(CurrentUser.id(), questionId);
        return ResponseEntity.noContent().build();
    }
}
