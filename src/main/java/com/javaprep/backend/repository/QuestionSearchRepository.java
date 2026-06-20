package com.javaprep.backend.repository;

import com.javaprep.backend.entity.Question;
import com.javaprep.backend.entity.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionSearchRepository {

    /**
     * Flexible multi-filter search backing GET /api/questions.
     * Any parameter may be null/blank, in which case it is not applied.
     */
    Page<Question> search(
            QuestionType questionType,
            String topic,
            String category,
            String difficulty,
            String priority,
            String tag,
            String company,
            String searchText,
            String week,      // <--- ADD THIS LINE
            Pageable pageable
    );
}
