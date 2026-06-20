package com.javaprep.backend.service;

import com.javaprep.backend.dto.cheatsheet.CheatSheetItemRequest;
import com.javaprep.backend.dto.cheatsheet.CheatSheetItemResponse;

import java.util.List;
import java.util.Map;

public interface CheatSheetService {

    /** All items grouped by category, in display order — matches the original page's grouped layout. */
    Map<String, List<CheatSheetItemResponse>> listGroupedByCategory();

    List<CheatSheetItemResponse> listAll();

    CheatSheetItemResponse create(CheatSheetItemRequest request);

    CheatSheetItemResponse update(String id, CheatSheetItemRequest request);

    void delete(String id);
}
