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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserProgressRepository userProgressRepository;
    private final QuestionMapper questionMapper;
    private final MongoTemplate mongoTemplate;

    // Safely injects cache manager if available, preventing startup crashes
    private final ObjectProvider<CacheManager> cacheManagerProvider;

    private static final String CACHE_NAME = "allQuestions";
    private static final String CACHE_KEY = "globalList";

    /**
     * Core In-Memory Cache Loader
     * Hits DB once, then serves from RAM.
     */
    @SuppressWarnings("unchecked")
    public List<Question> getAllQuestionsForCache() {
        CacheManager cacheManager = cacheManagerProvider.getIfAvailable();
        if (cacheManager != null) {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                List<Question> cached = cache.get(CACHE_KEY, List.class);
                if (cached != null) return cached;
            }
        }

        log.info("🔥 Loading all questions from MongoDB into JVM Memory...");
        List<Question> dbList = questionRepository.findAll();

        if (cacheManager != null) {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                cache.put(CACHE_KEY, dbList);
            }
        }
        return dbList;
    }

    /**
     * Clears the cache on any Write operation
     */
    private void evictCache() {
        CacheManager cacheManager = cacheManagerProvider.getIfAvailable();
        if (cacheManager != null) {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                cache.evict(CACHE_KEY);
                log.info("🧹 Question cache evicted due to data update.");
            }
        }
    }

    @Override
    @Transactional
    public QuestionResponse getById(String id, String currentUserIdOrNull) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Question", id));

        // Increment view count directly in DB without breaking cache
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

        // 1. Instantly pull from RAM
        List<Question> allQuestions = getAllQuestionsForCache();

        // 2. Filter in-memory (0ms latency)
        Stream<Question> stream = allQuestions.stream();

        if (questionType != null) stream = stream.filter(q -> q.getQuestionType() == questionType);
        if (topic != null) stream = stream.filter(q -> topic.equalsIgnoreCase(q.getTopic()));
        if (category != null) stream = stream.filter(q -> category.equalsIgnoreCase(q.getCategory()));
        if (difficulty != null) stream = stream.filter(q -> difficulty.equalsIgnoreCase(q.getDifficulty()));
        if (priority != null) stream = stream.filter(q -> priority.equalsIgnoreCase(q.getPriority()));

        if (tag != null) stream = stream.filter(q -> q.getTags() != null &&
                q.getTags().stream().anyMatch(t -> t.equalsIgnoreCase(tag)));

        if (company != null) stream = stream.filter(q -> q.getCompanyAskedIn() != null &&
                q.getCompanyAskedIn().stream().anyMatch(c -> c.equalsIgnoreCase(company)));

        if (week != null) stream = stream.filter(q -> week.equalsIgnoreCase(q.getWeek()));

        if (search != null && !search.trim().isEmpty()) {
            String lowerSearch = search.toLowerCase();
            stream = stream.filter(q -> q.getTitle() != null && q.getTitle().toLowerCase().contains(lowerSearch));
        }

        List<Question> filtered = stream.collect(Collectors.toList());

        // 3. Sort
        if (pageable.getSort().isSorted()) {
            filtered.sort(getComparator(pageable.getSort()));
        } else {
            // Default sort: newest first
            filtered.sort((q1, q2) -> compareNullable(q2.getCreatedAt(), q1.getCreatedAt()));
        }

        // 4. Paginate
        int total = filtered.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), total);

        List<Question> pageContent = (start <= end && start < total)
                ? filtered.subList(start, end)
                : Collections.emptyList();

        // 5. Map and Enrich (Only executes ~10 DB calls instead of a full table scan)
        List<QuestionResponse> responses = pageContent.stream()
                .map(q -> {
                    QuestionResponse r = questionMapper.toResponse(q);
                    enrichWithUserContext(r, currentUserIdOrNull);
                    return r;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, total);
    }

    @Override
    public List<QuestionResponse> findByTag(String tag) {
        return getAllQuestionsForCache().stream()
                .filter(q -> q.getStatus() == QuestionStatus.PUBLISHED)
                .filter(q -> q.getTags() != null && q.getTags().stream().anyMatch(t -> t.equalsIgnoreCase(tag)))
                .map(questionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuestionResponse> findByTopic(String topic) {
        return getAllQuestionsForCache().stream()
                .filter(q -> q.getStatus() == QuestionStatus.PUBLISHED)
                .filter(q -> topic.equalsIgnoreCase(q.getTopic()))
                .map(questionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuestionResponse> findByCompany(String company) {
        return getAllQuestionsForCache().stream()
                .filter(q -> q.getStatus() == QuestionStatus.PUBLISHED)
                .filter(q -> q.getCompanyAskedIn() != null && q.getCompanyAskedIn().stream().anyMatch(c -> c.equalsIgnoreCase(company)))
                .map(questionMapper::toResponse)
                .collect(Collectors.toList());
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

        clearGlobalQuestionCache(); // Keep cache fresh
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

        clearGlobalQuestionCache(); // Keep cache fresh
        return questionMapper.toResponse(existing);
    }

    @Override
    @Transactional
    public void delete(String id, String adminUserId) {
        if (!questionRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Question", id);
        }
        questionRepository.deleteById(id);
        clearGlobalQuestionCache(); // Keep cache fresh
    }

    @Override
    public QuestionResponse getRandomForMockInterview(String currentUserIdOrNull, List<String> excludeIds) {
        List<Question> candidates = getAllQuestionsForCache().stream()
                .filter(q -> q.getQuestionType() == QuestionType.THEORY && q.getStatus() == QuestionStatus.PUBLISHED)
                .collect(Collectors.toList());

        List<Question> filtered = candidates.stream()
                .filter(q -> excludeIds == null || !excludeIds.contains(q.getId()))
                .collect(Collectors.toList());

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
        return getAllQuestionsForCache().stream()
                .filter(q -> q.getQuestionType() == QuestionType.THEORY)
                .map(Question::getTopic)
                .filter(t -> t != null && !t.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<String>> getFilterMetadata() {
        Map<String, List<String>> meta = new HashMap<>();
        List<Question> all = getAllQuestionsForCache();

        meta.put("weeks", getDistinctSorted(all, QuestionType.DSA, Question::getWeek));
        meta.put("categories", getDistinctSorted(all, QuestionType.SYSTEM_DESIGN, Question::getCategory));

        meta.put("tags", all.stream()
                .map(Question::getTags)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(t -> t != null && !t.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList()));

        meta.put("difficulties", getDistinctSorted(all, null, Question::getDifficulty));
        meta.put("priorities", getDistinctSorted(all, null, Question::getPriority));

        return meta;
    }

    // Helper for distinct extractions from cache
    private List<String> getDistinctSorted(List<Question> all, QuestionType type, Function<Question, String> extractor) {
        return all.stream()
                .filter(q -> type == null || q.getQuestionType() == type)
                .map(extractor)
                .filter(v -> v != null && !v.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // Dynamic sorting comparator
    private Comparator<Question> getComparator(Sort sort) {
        Comparator<Question> comparator = (q1, q2) -> 0;
        for (Sort.Order order : sort) {
            Comparator<Question> propComp = (q1, q2) -> {
                try {
                    switch (order.getProperty()) {
                        case "createdAt": return compareNullable(q1.getCreatedAt(), q2.getCreatedAt());
                        case "topic": return compareNullable(q1.getTopic(), q2.getTopic());
                        case "id": return compareNullable(q1.getId(), q2.getId());
                        case "difficulty": return compareNullable(q1.getDifficulty(), q2.getDifficulty());
                        default: return 0;
                    }
                } catch (Exception e) {
                    return 0; // Safe fallback
                }
            };
            if (order.isDescending()) {
                propComp = propComp.reversed();
            }
            comparator = comparator.thenComparing(propComp);
        }
        return comparator;
    }

    // Null-safe comparison helper
    private <T extends Comparable<T>> int compareNullable(T a, T b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    @Override
    @CacheEvict(value = "allQuestions", allEntries = true)
    public void clearGlobalQuestionCache() {
        log.info("🧹 Global Question Cache forcefully cleared!");
    }
}