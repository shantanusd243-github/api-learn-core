package com.javaprep.backend.dto.dashboard;

import com.javaprep.backend.entity.QuestionType;
import com.javaprep.backend.enums.Priority;
import lombok.Data;
import java.util.List;

@Data
public class JdAnalysisResponse {
    private String role;
    private String difficulty;
    private String strategyMessage;
    private List<RadarItem> radarData;
    private List<String> expectations;
    private List<FocusArea> focusAreas;
    private boolean validJd;
    private String failureMessage;

    @Data
    public static class RadarItem {
        private String topic;
        private Priority level;
        private QuestionType module;
    }

    @Data
    public static class FocusArea {
        private String title;
        private String description;
        private QuestionType module;
    }
}