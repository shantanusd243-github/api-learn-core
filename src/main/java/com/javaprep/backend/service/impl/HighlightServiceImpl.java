package com.javaprep.backend.service.impl;

import com.javaprep.backend.entity.TextHighlight;
import com.javaprep.backend.repository.HighlightRepository;
import com.javaprep.backend.service.HighlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HighlightServiceImpl implements HighlightService {

    private final HighlightRepository highlightRepository;

    @Override
    public TextHighlight saveHighlight(String userId, TextHighlight request) {
        request.setUserId(userId);
        request.setCreatedAt(LocalDateTime.now());
        return highlightRepository.save(request);
    }

    @Override
    public List<TextHighlight> getHighlights(String userId, String targetId) {
        return highlightRepository.findByUserIdAndTargetId(userId, targetId);
    }

    @Override
    public void deleteHighlight(String id) {
        highlightRepository.deleteById(id);
    }
}