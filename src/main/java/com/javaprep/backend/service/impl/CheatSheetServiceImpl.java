package com.javaprep.backend.service.impl;

import com.javaprep.backend.dto.cheatsheet.CheatSheetItemRequest;
import com.javaprep.backend.dto.cheatsheet.CheatSheetItemResponse;
import com.javaprep.backend.entity.CheatSheetItem;
import com.javaprep.backend.exception.ResourceNotFoundException;
import com.javaprep.backend.repository.CheatSheetItemRepository;
import com.javaprep.backend.service.CheatSheetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheatSheetServiceImpl implements CheatSheetService {

    private final CheatSheetItemRepository cheatSheetItemRepository;

    @Cacheable(value = "cheatSheet", key = "'globalList'")
    public List<CheatSheetItem> getAllForCache() {
        log.info("🔥 Loading all Cheat Sheets into JVM RAM...");
        return cheatSheetItemRepository.findAll();
    }

    @Override
    public Map<String, List<CheatSheetItemResponse>> listGroupedByCategory() {
        List<CheatSheetItem> all = getAllForCache().stream()
                .sorted(Comparator.comparing(CheatSheetItem::getDisplayOrder))
                .toList();

        Map<String, List<CheatSheetItemResponse>> grouped = new LinkedHashMap<>();
        for (CheatSheetItem item : all) {
            grouped.computeIfAbsent(item.getCategory(), k -> new java.util.ArrayList<>())
                    .add(toResponse(item));
        }
        return grouped;
    }

    @Override
    @Cacheable(value = "cheatSheet", key = "'all'")
    public List<CheatSheetItemResponse> listAll() {
        return cheatSheetItemRepository.findAllByOrderByDisplayOrderAsc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "cheatSheet", allEntries = true)
    public CheatSheetItemResponse create(CheatSheetItemRequest request) {
        CheatSheetItem item = CheatSheetItem.builder()
                .category(request.getCategory())
                .categoryLabel(request.getCategoryLabel())
                .categoryIcon(request.getCategoryIcon())
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .displayOrder(request.getDisplayOrder())
                .build();
        return toResponse(cheatSheetItemRepository.save(item));
    }

    @Override
    @CacheEvict(value = "cheatSheet", allEntries = true)
    public CheatSheetItemResponse update(String id, CheatSheetItemRequest request) {
        CheatSheetItem item = cheatSheetItemRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("CheatSheetItem", id));
        item.setCategory(request.getCategory());
        item.setCategoryLabel(request.getCategoryLabel());
        item.setCategoryIcon(request.getCategoryIcon());
        item.setQuestion(request.getQuestion());
        item.setAnswer(request.getAnswer());
        item.setDisplayOrder(request.getDisplayOrder());
        return toResponse(cheatSheetItemRepository.save(item));
    }

    @Override
    @CacheEvict(value = "cheatSheet", allEntries = true)
    public void delete(String id) {
        if (!cheatSheetItemRepository.existsById(id)) {
            throw ResourceNotFoundException.of("CheatSheetItem", id);
        }
        cheatSheetItemRepository.deleteById(id);
    }

    private CheatSheetItemResponse toResponse(CheatSheetItem item) {
        return CheatSheetItemResponse.builder()
                .id(item.getId())
                .category(item.getCategory())
                .categoryLabel(item.getCategoryLabel())
                .categoryIcon(item.getCategoryIcon())
                .question(item.getQuestion())
                .answer(item.getAnswer())
                .displayOrder(item.getDisplayOrder())
                .build();
    }
}
