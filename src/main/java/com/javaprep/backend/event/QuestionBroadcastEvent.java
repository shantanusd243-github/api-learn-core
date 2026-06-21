package com.javaprep.backend.event;

import com.javaprep.backend.entity.Question;

public record QuestionBroadcastEvent(Question question) {}