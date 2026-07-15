package com.javaprep.backend.repository;

import com.javaprep.backend.entity.Question;
import com.javaprep.backend.entity.QuestionStatus;
import com.javaprep.backend.entity.QuestionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Fragment implementation backing {@link QuestionSearchRepository}. Spring Data MongoDB
 * composes this into the {@link QuestionRepository} bean automatically because this class
 * lives in the same package as the repository interface and follows the
 * "{RepositoryInterfaceName}Impl" naming convention.
 *
 * Only PUBLISHED questions are searchable from this method — it backs the public-facing
 * GET /api/questions endpoint. Admin-only listing of DRAFT/ARCHIVED content goes through
 * separate repository methods, not this search.
 */
@Repository
@RequiredArgsConstructor
public class QuestionRepositoryImpl implements QuestionSearchRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Question> search(
            QuestionType questionType,
            String topic,
            String category,
            String difficulty,
            String priority,
            String tag,
            String company,
            String searchText,
            String week,      // <--- 1. ADDED WEEK TO SIGNATURE
            Pageable pageable
    ) {
        List<Criteria> criteriaList = new ArrayList<>();

        // Default to PUBLISHED-only for the public search surface.
        criteriaList.add(Criteria.where("status").is(QuestionStatus.PUBLISHED));

        if (questionType != null) {
            criteriaList.add(Criteria.where("questionType").is(questionType));
        }
        if (StringUtils.hasText(topic)) {
            criteriaList.add(Criteria.where("topic").regex(Pattern.quote(topic), "i"));
        }
        if (StringUtils.hasText(category)) {
            criteriaList.add(Criteria.where("category").regex(Pattern.quote(category), "i"));
        }
        if (StringUtils.hasText(difficulty)) {
            criteriaList.add(Criteria.where("difficulty").regex(Pattern.quote(difficulty), "i"));
        }
        if (StringUtils.hasText(priority)) {
            criteriaList.add(Criteria.where("priority").regex(Pattern.quote(priority), "i"));
        }
        if (StringUtils.hasText(tag)) {
            criteriaList.add(Criteria.where("tags").regex(Pattern.quote(tag), "i"));
        }
        if (StringUtils.hasText(company)) {
            criteriaList.add(Criteria.where("companyAskedIn").regex(Pattern.quote(company), "i"));
        }

        if (StringUtils.hasText(week)) {
            criteriaList.add(Criteria.where("week").regex(Pattern.quote(week), "i"));
        }

        if (StringUtils.hasText(searchText)) {
            String quoted = Pattern.quote(searchText);
            Criteria textCriteria = new Criteria().orOperator(
                    Criteria.where("title").regex(quoted, "i"),
                    Criteria.where("shortSummary").regex(quoted, "i"),
                    Criteria.where("answer").regex(quoted, "i"),
                    Criteria.where("tags").regex(quoted, "i")
            );
            criteriaList.add(textCriteria);
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Question.class);

        query.with(pageable);
        List<Question> results = mongoTemplate.find(query, Question.class);

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public List<Question> getRandomFilteredQuestions(int count, String topic, String company, String difficulty) {
        List<Criteria> criteriaList = new ArrayList<>();

        // 1. MUST ONLY PULL PUBLISHED QUESTIONS (Crucial for mock interviews)
        criteriaList.add(Criteria.where("status").is(QuestionStatus.PUBLISHED));

        // 2. Fix the field names to match your schema
        if (StringUtils.hasText(topic)) {
            if(!topic.equalsIgnoreCase("DSA") && !topic.equalsIgnoreCase("System Design"))
            {
                criteriaList.add(Criteria.where("topic").regex(Pattern.quote(topic), "i"));
            }
            else if(topic.equalsIgnoreCase("DSA")){
                criteriaList.add(Criteria.where("questionType").regex(Pattern.quote("DSA"), "i"));
            }
            else if(topic.equalsIgnoreCase("System Design")){
                criteriaList.add(Criteria.where("questionType").regex(Pattern.quote("SYSTEM_DESIGN"), "i"));
            }

        }
        if (StringUtils.hasText(company)) {
            criteriaList.add(Criteria.where("companyAskedIn").regex(Pattern.quote(company), "i"));
        }
        if (StringUtils.hasText(difficulty)) {
            criteriaList.add(Criteria.where("difficulty").regex(Pattern.quote(difficulty), "i"));
        }

        // 3. Assemble the query
        MatchOperation matchOperation = Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        SampleOperation sampleOperation = Aggregation.sample(count);

        Aggregation aggregation = Aggregation.newAggregation(matchOperation, sampleOperation);
        AggregationResults<Question> results = mongoTemplate.aggregate(aggregation, "questions", Question.class);

        return results.getMappedResults();
    }
}
