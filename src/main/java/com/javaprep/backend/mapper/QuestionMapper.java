package com.javaprep.backend.mapper;

import com.javaprep.backend.dto.question.QuestionRequest;
import com.javaprep.backend.dto.question.QuestionResponse;
import com.javaprep.backend.entity.Question;
import com.javaprep.backend.entity.QuestionStatus;
import org.springframework.stereotype.Component;

@Component
public class QuestionMapper {

    public QuestionResponse toResponse(Question q) {
        if (q == null) return null;
        return QuestionResponse.builder()
                .id(q.getId())
                .questionType(q.getQuestionType())
                .status(q.getStatus())
                .topic(q.getTopic())
                .category(q.getCategory())
                .priority(q.getPriority())
                .difficulty(q.getDifficulty())
                .tags(q.getTags())
                .companyAskedIn(q.getCompanyAskedIn())
                .shortSummary(q.getShortSummary())
                .viewCount(q.getViewCount())
                .bookmarkCount(q.getBookmarkCount())
                .usefulVoteCount(q.getUsefulVoteCount())
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .title(q.getTitle())
                .answer(q.getAnswer())
                .deep(q.getDeepExplanation())
                .followup(q.getFollowup())
                .real(q.getRealWorldUsage())
                .week(q.getWeek())
                .time(q.getTimeEstimate())
                .intro(q.getIntro())
                .intuition(q.getIntuition())
                .approach(q.getApproach())
                .example(q.getExample())
                .code(q.getCode())
                .timeC(q.getTimeComplexity())
                .spaceC(q.getSpaceComplexity())
                .edges(q.getEdgeCases())
                .talk(q.getTalkTrack())
                .followups(q.getFollowups())
                .problem(q.getProblemStatement())
                .requirements(q.getRequirements())
                .functionalRequirements(q.getFunctionalRequirements())
                .nonFunctionalRequirements(q.getNonFunctionalRequirements())
                .design(q.getHighLevelDesign())
                .api(q.getApiDesign())
                .dbDesign(q.getDbDesign())
                .scaling(q.getScalingStrategy())
                .cachingStrategy(q.getCachingStrategy())
                .consistencyTradeoffs(q.getConsistencyTradeoffs())
                .failureScenarios(q.getFailureScenarios())
                .observability(q.getObservability())
                .security(q.getSecurity())
                .tradeoffs(q.getTradeoffs())
                .diagramMarkdown(q.getDiagramMarkdown())
                .build();
    }

    public Question toEntity(QuestionRequest req) {
        return Question.builder()
                .questionType(req.getQuestionType())
                .status(req.getStatus() != null ? req.getStatus() : QuestionStatus.DRAFT)
                .title(req.getTitle())
                .topic(req.getTopic())
                .category(req.getCategory())
                .priority(req.getPriority())
                .difficulty(req.getDifficulty())
                .tags(req.getTags())
                .companyAskedIn(req.getCompanyAskedIn())
                .shortSummary(req.getShortSummary())
                .viewCount(0)
                .bookmarkCount(0)
                .usefulVoteCount(0)
                .answer(req.getAnswer())
                .deepExplanation(req.getDeep())
                .followup(req.getFollowup())
                .realWorldUsage(req.getReal())
                .week(req.getWeek())
                .timeEstimate(req.getTime())
                .intro(req.getIntro())
                .intuition(req.getIntuition())
                .approach(req.getApproach())
                .example(req.getExample())
                .code(req.getCode())
                .timeComplexity(req.getTimeC())
                .spaceComplexity(req.getSpaceC())
                .edgeCases(req.getEdges())
                .talkTrack(req.getTalk())
                .followups(req.getFollowups())
                .problemStatement(req.getProblem())
                .requirements(req.getRequirements())
                .functionalRequirements(req.getFunctionalRequirements())
                .nonFunctionalRequirements(req.getNonFunctionalRequirements())
                .highLevelDesign(req.getDesign())
                .apiDesign(req.getApi())
                .dbDesign(req.getDbDesign())
                .scalingStrategy(req.getScaling())
                .cachingStrategy(req.getCachingStrategy())
                .consistencyTradeoffs(req.getConsistencyTradeoffs())
                .failureScenarios(req.getFailureScenarios())
                .observability(req.getObservability())
                .security(req.getSecurity())
                .tradeoffs(req.getTradeoffs())
                .diagramMarkdown(req.getDiagramMarkdown())
                .build();
    }

    /** Applies all mutable fields from a request onto an existing entity (used by PUT/update). */
    public void applyUpdate(Question target, QuestionRequest req) {
        target.setQuestionType(req.getQuestionType());
        if (req.getStatus() != null) target.setStatus(req.getStatus());
        target.setTitle(req.getTitle());
        target.setTopic(req.getTopic());
        target.setCategory(req.getCategory());
        target.setPriority(req.getPriority());
        target.setDifficulty(req.getDifficulty());
        target.setTags(req.getTags());
        target.setCompanyAskedIn(req.getCompanyAskedIn());
        target.setShortSummary(req.getShortSummary());

        target.setAnswer(req.getAnswer());
        target.setDeepExplanation(req.getDeep());
        target.setFollowup(req.getFollowup());
        target.setRealWorldUsage(req.getReal());

        target.setWeek(req.getWeek());
        target.setTimeEstimate(req.getTime());
        target.setIntro(req.getIntro());
        target.setIntuition(req.getIntuition());
        target.setApproach(req.getApproach());
        target.setExample(req.getExample());
        target.setCode(req.getCode());
        target.setTimeComplexity(req.getTimeC());
        target.setSpaceComplexity(req.getSpaceC());
        target.setEdgeCases(req.getEdges());
        target.setTalkTrack(req.getTalk());
        target.setFollowups(req.getFollowups());

        target.setProblemStatement(req.getProblem());
        target.setRequirements(req.getRequirements());
        target.setFunctionalRequirements(req.getFunctionalRequirements());
        target.setNonFunctionalRequirements(req.getNonFunctionalRequirements());
        target.setHighLevelDesign(req.getDesign());
        target.setApiDesign(req.getApi());
        target.setDbDesign(req.getDbDesign());
        target.setScalingStrategy(req.getScaling());
        target.setCachingStrategy(req.getCachingStrategy());
        target.setConsistencyTradeoffs(req.getConsistencyTradeoffs());
        target.setFailureScenarios(req.getFailureScenarios());
        target.setObservability(req.getObservability());
        target.setSecurity(req.getSecurity());
        target.setTradeoffs(req.getTradeoffs());
        target.setDiagramMarkdown(req.getDiagramMarkdown());
    }
}
