package com.javaprep.backend.repository;

import com.javaprep.backend.entity.CheatSheetItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CheatSheetItemRepository extends MongoRepository<CheatSheetItem, String> {

    List<CheatSheetItem> findAllByOrderByDisplayOrderAsc();

    List<CheatSheetItem> findByCategoryOrderByDisplayOrderAsc(String category);
}
