package com.javaprep.backend.service.impl;

import com.javaprep.backend.dto.reference.ReferenceContentRequest;
import com.javaprep.backend.dto.reference.ReferenceContentResponse;
import com.javaprep.backend.entity.ReferenceContent;
import com.javaprep.backend.exception.DuplicateResourceException;
import com.javaprep.backend.exception.ResourceNotFoundException;
import com.javaprep.backend.repository.ReferenceContentRepository;
import com.javaprep.backend.service.ReferenceContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReferenceContentServiceImpl implements ReferenceContentService {

    private final ReferenceContentRepository referenceContentRepository;

    @Override
    @Cacheable(value = "referenceContent", key = "'all'")
    public List<ReferenceContentResponse> listAll() {
        return referenceContentRepository.findAllByOrderByDisplayOrderAsc()
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Cacheable(value = "referenceContent", key = "#pageKey")
    public ReferenceContentResponse getByPageKey(String pageKey) {
        ReferenceContent content = referenceContentRepository.findByPageKey(pageKey)
                .orElseThrow(() -> ResourceNotFoundException.of("ReferenceContent", pageKey));
        return toResponse(content);
    }

    @Override
    @CacheEvict(value = "referenceContent", allEntries = true)
    public ReferenceContentResponse create(ReferenceContentRequest request, String adminUserId) {
        if (referenceContentRepository.existsByPageKey(request.getPageKey())) {
            throw new DuplicateResourceException(
                    "Reference content with pageKey '" + request.getPageKey() + "' already exists");
        }
        ReferenceContent content = ReferenceContent.builder()
                .pageKey(request.getPageKey())
                .icon(request.getIcon())
                .title(request.getTitle())
                .description(request.getDescription())
                .bodyHtml(request.getBodyHtml())
                .displayOrder(request.getDisplayOrder())
                .updatedBy(adminUserId)
                .build();
        return toResponse(referenceContentRepository.save(content));
    }

    @Override
    @CacheEvict(value = "referenceContent", allEntries = true)
    public ReferenceContentResponse update(String pageKey, ReferenceContentRequest request, String adminUserId) {
        ReferenceContent content = referenceContentRepository.findByPageKey(pageKey)
                .orElseThrow(() -> ResourceNotFoundException.of("ReferenceContent", pageKey));

        content.setIcon(request.getIcon());
        content.setTitle(request.getTitle());
        content.setDescription(request.getDescription());
        content.setBodyHtml(request.getBodyHtml());
        content.setDisplayOrder(request.getDisplayOrder());
        content.setUpdatedBy(adminUserId);

        return toResponse(referenceContentRepository.save(content));
    }

    @Override
    @CacheEvict(value = "referenceContent", allEntries = true)
    public void delete(String pageKey) {
        ReferenceContent content = referenceContentRepository.findByPageKey(pageKey)
                .orElseThrow(() -> ResourceNotFoundException.of("ReferenceContent", pageKey));
        referenceContentRepository.delete(content);
    }

    private ReferenceContentResponse toResponse(ReferenceContent c) {
        return ReferenceContentResponse.builder()
                .id(c.getId())
                .pageKey(c.getPageKey())
                .icon(c.getIcon())
                .title(c.getTitle())
                .description(c.getDescription())
                .bodyHtml(c.getBodyHtml())
                .displayOrder(c.getDisplayOrder())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
