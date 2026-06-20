package com.javaprep.backend.service.impl;

import com.javaprep.backend.dto.question.QuestionRequest;
import com.javaprep.backend.dto.question.QuestionResponse;
import com.javaprep.backend.entity.Question;
import com.javaprep.backend.entity.QuestionStatus;
import com.javaprep.backend.entity.QuestionType;
import com.javaprep.backend.entity.UserProgress;
import com.javaprep.backend.exception.ResourceNotFoundException;
import com.javaprep.backend.mapper.QuestionMapper;
import com.javaprep.backend.repository.BookmarkRepository;
import com.javaprep.backend.repository.QuestionRepository;
import com.javaprep.backend.repository.UserProgressRepository;
import com.javaprep.backend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserProgressRepository userProgressRepository;
    private final QuestionMapper questionMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional
    public QuestionResponse getById(String id, String currentUserIdOrNull) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Question", id));

        // increment view count without re-saving the whole document (avoids overwriting concurrent edits)
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(id)),
                new Update().inc("viewCount", 1),
                Question.class
        );

        QuestionResponse response = questionMapper.toResponse(question);
        enrichWithUserContext(response, currentUserIdOrNull);
        return response;
    }

    @Override
    public Page<QuestionResponse> search(QuestionType questionType, String topic, String category, String difficulty,
                                         String priority, String tag, String company, String search, String week,
                                         Pageable pageable, String currentUserIdOrNull) {

        // Pass 'week' down to the repository layer
        Page<Question> page = questionRepository.search(
                questionType, topic, category, difficulty, priority, tag, company, search, week, pageable);

        return page.map(q -> {
            QuestionResponse r = questionMapper.toResponse(q);
            enrichWithUserContext(r, currentUserIdOrNull);
            return r;
        });
    }

    @Override
    public List<QuestionResponse> findByTag(String tag) {
        return questionRepository.findByTagsContainingAndStatus(tag, QuestionStatus.PUBLISHED)
                .stream().map(questionMapper::toResponse).toList();
    }

    @Override
    public List<QuestionResponse> findByTopic(String topic) {
        return questionRepository.findByTopicAndStatus(topic, QuestionStatus.PUBLISHED)
                .stream().map(questionMapper::toResponse).toList();
    }

    @Override
    public List<QuestionResponse> findByCompany(String company) {
        return questionRepository.findByCompanyAskedInContainingAndStatus(company, QuestionStatus.PUBLISHED)
                .stream().map(questionMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public QuestionResponse create(QuestionRequest request, String adminUserId) {
        Question question = questionMapper.toEntity(request);
        question.setCreatedBy(adminUserId);
        question.setUpdatedBy(adminUserId);
        question.setCreatedAt(Instant.now());
        question.setUpdatedAt(Instant.now());
        question = questionRepository.save(question);
        return questionMapper.toResponse(question);
    }

    @Override
    @Transactional
    public QuestionResponse update(String id, QuestionRequest request, String adminUserId) {
        Question existing = questionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Question", id));

        questionMapper.applyUpdate(existing, request);
        existing.setUpdatedBy(adminUserId);
        existing.setUpdatedAt(Instant.now());

        existing = questionRepository.save(existing);
        return questionMapper.toResponse(existing);
    }

    @Override
    @Transactional
    public void delete(String id, String adminUserId) {
        if (!questionRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Question", id);
        }
        questionRepository.deleteById(id);
    }

    @Override
    public QuestionResponse getRandomForMockInterview(String currentUserIdOrNull, List<String> excludeIds) {
        List<Question> candidates = questionRepository.findByQuestionTypeAndStatus(
                QuestionType.THEORY, QuestionStatus.PUBLISHED);

        List<Question> filtered = candidates.stream()
                .filter(q -> excludeIds == null || !excludeIds.contains(q.getId()))
                .toList();

        List<Question> pool = filtered.isEmpty() ? candidates : filtered;
        if (pool.isEmpty()) {
            throw new ResourceNotFoundException("No published theory questions available for mock interview");
        }

        Question picked = pool.get(new Random().nextInt(pool.size()));
        QuestionResponse response = questionMapper.toResponse(picked);
        enrichWithUserContext(response, currentUserIdOrNull);
        return response;
    }

    private void enrichWithUserContext(QuestionResponse response, String userIdOrNull) {
        if (userIdOrNull == null) {
            return;
        }
        response.setBookmarked(bookmarkRepository.existsByUserIdAndQuestionId(userIdOrNull, response.getId()));
        userProgressRepository.findByUserIdAndQuestionId(userIdOrNull, response.getId())
                .map(UserProgress::getStatus)
                .ifPresent(status -> response.setUserProgressStatus(status.name()));
    }

    @Override
    public List<String> getAllTopics() {
        // THE FIX: Only fetch topics where the question is a THEORY question!
        Query query = new Query();
        query.addCriteria(Criteria.where("questionType").is(QuestionType.THEORY));

        List<String> topics = mongoTemplate.findDistinct(query, "topic", Question.class, String.class);
        topics.removeIf(t -> t == null || t.trim().isEmpty());
        Collections.sort(topics);
        return topics;
    }

    @Override
    public Map<String, List<String>> getFilterMetadata() {
        Map<String, List<String>> meta = new HashMap<>();

        // Dynamically fetch unique values directly from the database
        meta.put("weeks", getDistinctSorted("week", QuestionType.DSA));
        meta.put("categories", getDistinctSorted("category", QuestionType.SYSTEM_DESIGN));
        meta.put("tags", getDistinctSorted("tags", null)); // Get all tags across all types
        meta.put("difficulties", getDistinctSorted("difficulty", null));
        meta.put("priorities", getDistinctSorted("priority", null));

        return meta;
    }

    // Helper method to keep code clean
    private List<String> getDistinctSorted(String field, QuestionType type) {
        Query query = new Query();
        if (type != null) {
            query.addCriteria(Criteria.where("questionType").is(type));
        }
        List<String> list = mongoTemplate.findDistinct(query, field, Question.class, String.class);
        list.removeIf(v -> v == null || v.trim().isEmpty());
        Collections.sort(list);
        return list;
    }

}
