package com.javaprep.backend.service.impl;

import com.javaprep.backend.dto.common.TaxonomyItemResponse;
import com.javaprep.backend.dto.company.CompanyRequest;
import com.javaprep.backend.dto.tag.TagRequest;
import com.javaprep.backend.dto.topic.TopicRequest;
import com.javaprep.backend.entity.Company;
import com.javaprep.backend.entity.Tag;
import com.javaprep.backend.entity.Topic;
import com.javaprep.backend.exception.DuplicateResourceException;
import com.javaprep.backend.exception.ResourceNotFoundException;
import com.javaprep.backend.repository.CompanyRepository;
import com.javaprep.backend.repository.TagRepository;
import com.javaprep.backend.repository.TopicRepository;
import com.javaprep.backend.service.TaxonomyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxonomyServiceImpl implements TaxonomyService {

    private final TopicRepository topicRepository;
    private final TagRepository tagRepository;
    private final CompanyRepository companyRepository;

    // ---------- Topics ----------

    @Override
    public List<TaxonomyItemResponse> listTopics() {
        return topicRepository.findAllByOrderByDisplayOrderAsc().stream().map(this::toResponse).toList();
    }

    @Override
    public TaxonomyItemResponse createTopic(TopicRequest request) {
        if (topicRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Topic '" + request.getName() + "' already exists");
        }
        Topic topic = Topic.builder()
                .name(request.getName())
                .icon(request.getIcon())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .build();
        return toResponse(topicRepository.save(topic));
    }

    @Override
    public TaxonomyItemResponse updateTopic(String id, TopicRequest request) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Topic", id));
        topic.setName(request.getName());
        topic.setIcon(request.getIcon());
        topic.setDescription(request.getDescription());
        topic.setDisplayOrder(request.getDisplayOrder());
        return toResponse(topicRepository.save(topic));
    }

    @Override
    public void deleteTopic(String id) {
        if (!topicRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Topic", id);
        }
        topicRepository.deleteById(id);
    }

    // ---------- Tags ----------

    @Override
    public List<TaxonomyItemResponse> listTags() {
        return tagRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public TaxonomyItemResponse createTag(TagRequest request) {
        if (tagRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Tag '" + request.getName() + "' already exists");
        }
        Tag tag = Tag.builder().name(request.getName()).usageCount(0).build();
        return toResponse(tagRepository.save(tag));
    }

    @Override
    public void deleteTag(String id) {
        if (!tagRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Tag", id);
        }
        tagRepository.deleteById(id);
    }

    // ---------- Companies ----------

    @Override
    public List<TaxonomyItemResponse> listCompanies() {
        return companyRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public TaxonomyItemResponse createCompany(CompanyRequest request) {
        if (companyRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Company '" + request.getName() + "' already exists");
        }
        Company company = Company.builder()
                .name(request.getName())
                .logoUrl(request.getLogoUrl())
                .usageCount(0)
                .build();
        return toResponse(companyRepository.save(company));
    }

    @Override
    public TaxonomyItemResponse updateCompany(String id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Company", id));
        company.setName(request.getName());
        company.setLogoUrl(request.getLogoUrl());
        return toResponse(companyRepository.save(company));
    }

    @Override
    public void deleteCompany(String id) {
        if (!companyRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Company", id);
        }
        companyRepository.deleteById(id);
    }

    // ---------- mapping helpers ----------

    private TaxonomyItemResponse toResponse(Topic t) {
        return TaxonomyItemResponse.builder()
                .id(t.getId()).name(t.getName()).icon(t.getIcon()).description(t.getDescription())
                .build();
    }

    private TaxonomyItemResponse toResponse(Tag t) {
        return TaxonomyItemResponse.builder()
                .id(t.getId()).name(t.getName()).usageCount(t.getUsageCount())
                .build();
    }

    private TaxonomyItemResponse toResponse(Company c) {
        return TaxonomyItemResponse.builder()
                .id(c.getId()).name(c.getName()).logoUrl(c.getLogoUrl()).usageCount(c.getUsageCount())
                .build();
    }
}
