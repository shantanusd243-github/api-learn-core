package com.javaprep.backend.dto.question;

import com.javaprep.backend.entity.QuestionStatus;
import com.javaprep.backend.entity.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {

    @NotNull(message = "questionType is required")
    private QuestionType questionType;

    private QuestionStatus status; // defaults to DRAFT in service if null

    @NotBlank(message = "title is required")
    private String title;

    private String topic;
    private String category;
    private String priority;
    private String difficulty;
    private List<String> tags;
    private List<String> companyAskedIn;
    private String shortSummary;

    // Theory
    private String answer;
    private String deep;
    private String followup;
    private String real;

    // DSA
    private String week;
    private String time;
    private String intro;
    private String intuition;
    private String approach;
    private String example;
    private String code;
    private String timeC;
    private String spaceC;
    private List<String> edges;
    private String talk;
    private List<String> followups;

    // System Design
    private String problem;
    private String requirements;
    private String functionalRequirements;
    private String nonFunctionalRequirements;
    private String design;
    private String api;
    private String dbDesign;
    private String scaling;
    private String cachingStrategy;
    private String consistencyTradeoffs;
    private String failureScenarios;
    private String observability;
    private String security;
    private String tradeoffs;
    private String diagramMarkdown;
}
