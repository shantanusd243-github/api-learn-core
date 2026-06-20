package com.javaprep.backend.service.impl;

import com.javaprep.backend.dto.bookmark.BookmarkResponse;
import com.javaprep.backend.entity.Bookmark;
import com.javaprep.backend.entity.Question;
import com.javaprep.backend.exception.ResourceNotFoundException;
import com.javaprep.backend.repository.BookmarkRepository;
import com.javaprep.backend.repository.QuestionRepository;
import com.javaprep.backend.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final QuestionRepository questionRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional
    public BookmarkResponse addBookmark(String userId, String questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw ResourceNotFoundException.of("Question", questionId);
        }

        Bookmark bookmark = bookmarkRepository.findByUserIdAndQuestionId(userId, questionId)
                .orElseGet(() -> {
                    Bookmark created = bookmarkRepository.save(Bookmark.builder()
                            .userId(userId)
                            .questionId(questionId)
                            .createdAt(Instant.now())
                            .build());
                    incrementBookmarkCount(questionId, 1);
                    return created;
                });

        return toResponse(bookmark);
    }

    @Override
    @Transactional
    public void removeBookmark(String userId, String questionId) {
        boolean existed = bookmarkRepository.existsByUserIdAndQuestionId(userId, questionId);
        bookmarkRepository.deleteByUserIdAndQuestionId(userId, questionId);
        if (existed) {
            incrementBookmarkCount(questionId, -1);
        }
    }

    @Override
    public List<BookmarkResponse> listBookmarks(String userId) {
        return bookmarkRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    private void incrementBookmarkCount(String questionId, int delta) {
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(questionId)),
                new Update().inc("bookmarkCount", delta),
                Question.class
        );
    }

    private BookmarkResponse toResponse(Bookmark b) {
        return BookmarkResponse.builder()
                .id(b.getId())
                .questionId(b.getQuestionId())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
