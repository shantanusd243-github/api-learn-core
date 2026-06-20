package com.javaprep.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Backs the static reference / strategy pages from the original app: Core Java,
 * Java 8, Spring Boot, Microservices, SQL Practice, Quick Cheat Sheet, Senior Gaps,
 * Revision Plan, and Story Prep.
 *
 * These pages are hand-authored, irregular layouts (tables, colored callouts,
 * multi-column grids) with no shared structure across pages — unlike Question,
 * which has a uniform card pattern. Rather than force them into a rigid schema
 * (which would require a bespoke renderer per page anyway, and risks the UI
 * drifting from the original), each page is stored as one document holding a
 * single rich HTML body. The frontend renders bodyHtml inside the same CSS
 * classes the static site used, so visual output is unchanged. Admins edit
 * bodyHtml as a single field instead of per-field forms.
 */
@Document(collection = "reference_content")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceContent {

    @Id
    private String id;

    /** Stable slug matching the original page id, e.g. "core-java", "plan", "cheat". */
    @Indexed(unique = true)
    private String pageKey;

    private String icon;       // e.g. "☕"
    private String title;      // e.g. "Core Java Quick Reference"
    private String description; // section-desc text

    /** Raw HTML body rendered verbatim inside .content > .page, preserving original markup. */
    private String bodyHtml;

    private int displayOrder;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private String updatedBy; // admin userId
}
