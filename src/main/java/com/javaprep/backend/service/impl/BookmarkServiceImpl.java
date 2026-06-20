package com.javaprep.backend.service.impl;

import com.javaprep.backend.dto.bookmark.BookmarkResponse;
import com.javaprep.backend.dto.question.QuestionResponse;
import com.javaprep.backend.entity.Bookmark;
import com.javaprep.backend.entity.Question;
import com.javaprep.backend.exception.ResourceNotFoundException;
import com.javaprep.backend.mapper.QuestionMapper;
import com.javaprep.backend.repository.BookmarkRepository;
import com.javaprep.backend.repository.QuestionRepository;
import com.javaprep.backend.service.BookmarkService;
import com.javaprep.backend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final QuestionRepository questionRepository;
    private final MongoTemplate mongoTemplate;
    private final QuestionService questionService;
    private final QuestionMapper questionMapper;

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

    @Override
    public List<QuestionResponse> getBookmarkedQuestionsDetails(String userId) {

        // 1. Fetch the user's bookmarked IDs from Mongo (Only 1 network call!)
        Set<String> bookmarkedIds = bookmarkRepository.findByUserId(userId)
                .stream()
                .map(Bookmark::getQuestionId)
                .collect(Collectors.toSet());

        if (bookmarkedIds.isEmpty()) {
            return List.of();
        }

        // 2. Instantly grab the global list from RAM (0 network calls)
        List<Question> allQuestions = questionService.getAllQuestionsForCache();

        // 3. Filter and map in memory (Lightning fast)
        return allQuestions.stream()
                .filter(q -> bookmarkedIds.contains(q.getId()))
                .map(q -> {
                    QuestionResponse r = questionMapper.toResponse(q);
                    r.setBookmarked(true); // We already know it's bookmarked!
                    return r;
                })
                .collect(Collectors.toList());
    }
}
