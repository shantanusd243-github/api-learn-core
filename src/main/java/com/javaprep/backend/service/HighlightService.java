package com.javaprep.backend.service;

import com.javaprep.backend.entity.TextHighlight;
import java.util.List;

public interface HighlightService {
    TextHighlight saveHighlight(String userId, TextHighlight request);
    List<TextHighlight> getHighlights(String userId, String targetId);
    void deleteHighlight(String id);
}