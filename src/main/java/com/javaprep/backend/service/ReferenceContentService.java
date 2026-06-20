package com.javaprep.backend.service;

import com.javaprep.backend.dto.reference.ReferenceContentRequest;
import com.javaprep.backend.dto.reference.ReferenceContentResponse;

import java.util.List;

public interface ReferenceContentService {

    List<ReferenceContentResponse> listAll();

    ReferenceContentResponse getByPageKey(String pageKey);

    ReferenceContentResponse create(ReferenceContentRequest request, String adminUserId);

    ReferenceContentResponse update(String pageKey, ReferenceContentRequest request, String adminUserId);

    void delete(String pageKey);
}
