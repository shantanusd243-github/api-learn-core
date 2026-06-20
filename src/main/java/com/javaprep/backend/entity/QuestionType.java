package com.javaprep.backend.entity;

/**
 * Discriminator for the unified `questions` collection.
 * A single collection is used (rather than 3 separate ones) because:
 *  - all three types share search/filter/bookmark/progress/mock-interview behavior
 *  - the frontend renders them through the same card/expand UI pattern
 *  - MongoDB's schemaless documents make a polymorphic "superset" document model
 *    cheap to maintain, while a discriminator field keeps queries/indexes simple
 */
public enum QuestionType {
    THEORY,         // regular interview questions (Core Java, Java 8, Spring Boot, etc.)
    DSA,            // data structure & algorithm problems
    SYSTEM_DESIGN   // system design problems
}
