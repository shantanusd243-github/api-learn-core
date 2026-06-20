package com.javaprep.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Backs the Quick Revision Cheat Sheet page. Unlike the other reference pages,
 * the original cheat sheet was driven by a structured {@code cheatData} JS object
 * (category -> list of {q, a} pairs) rather than hand-written HTML, so it gets a
 * proper schema instead of a raw-HTML blob.
 */
@Document(collection = "cheat_sheet_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheatSheetItem {

    @Id
    private String id;

    @Indexed
    private String category;       // e.g. "coreJava", "java8", "spring", "micro"
    private String categoryLabel;  // e.g. "Core Java Essentials"
    private String categoryIcon;   // e.g. "☕"

    private String question;
    private String answer;

    private int displayOrder;
}
