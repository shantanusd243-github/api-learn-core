package com.javaprep.backend.service;

import com.javaprep.backend.dto.common.TaxonomyItemResponse;
import com.javaprep.backend.dto.company.CompanyRequest;
import com.javaprep.backend.dto.tag.TagRequest;
import com.javaprep.backend.dto.topic.TopicRequest;

import java.util.List;

public interface TaxonomyService {

    // Topics
    List<TaxonomyItemResponse> listTopics();
    TaxonomyItemResponse createTopic(TopicRequest request);
    TaxonomyItemResponse updateTopic(String id, TopicRequest request);
    void deleteTopic(String id);

    // Tags
    List<TaxonomyItemResponse> listTags();
    TaxonomyItemResponse createTag(TagRequest request);
    void deleteTag(String id);

    // Companies
    List<TaxonomyItemResponse> listCompanies();
    TaxonomyItemResponse createCompany(CompanyRequest request);
    TaxonomyItemResponse updateCompany(String id, CompanyRequest request);
    void deleteCompany(String id);
}
