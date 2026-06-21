package com.javaprep.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaprep.backend.entity.*;
import com.javaprep.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;

/**
 * Optionally loads migrated static-HTML data into MongoDB on first boot, and
 * always ensures a bootstrap ADMIN account exists.
 *
 * IMPORTANT: the seed JSON files (seed-questions.json, seed-topics.json,
 * seed-tags.json, seed-reference-content.json, seed-cheatsheet.json) are
 * deliberately NOT bundled under src/main/seed on the classpath.
 * Per project decision, bulk data loading is done via a separate MongoDB
 * import script/query run against the target database directly, so the
 * person controls exactly when/how the 285 questions + reference pages +
 * cheat sheet items get loaded (rather than this happening implicitly on
 * every fresh deploy).
 *
 * Because of that, every seed* method here is best-effort: if its JSON file
 * isn't found on the classpath, it logs a single info line and moves on
 * instead of throwing — this is what actually matters, since a version of
 * this class that propagates a FileNotFoundException out of run() would
 * crash the entire application on startup. If you DO want auto-seeding,
 * just drop the corresponding seed-*.json file into
 * src/main/java/seed-data/ and rebuild; each method will pick it up
 * automatically and is idempotent (skips if the collection already has data).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;
    private final TagRepository tagRepository;
    private final CompanyRepository companyRepository;
    private final ReferenceContentRepository referenceContentRepository;
    private final CheatSheetItemRepository cheatSheetItemRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminBootstrapProperties adminBootstrapProperties;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) {
        seedTopics();
        seedTags();
        seedCompanies();
        seedQuestions();
        seedReferenceContent();
        seedCheatSheetItems();
        seedAdminUser(); // always runs, regardless of whether any seed file was found
    }

    private void seedTopics() {
       /* if (topicRepository.count() > 0) {
            log.info("Topics already seeded, skipping.");
            return;
        }*/
        Optional<List<Map<String, Object>>> raw = readJsonArray("seed-data/seed-topics.json");
        if (raw.isEmpty()) return;

        List<Topic> topics = new ArrayList<>();
        for (Map<String, Object> m : raw.get()) {
            topics.add(Topic.builder()
                    .name((String) m.get("name"))
                    .icon((String) m.get("icon"))
                    .description((String) m.get("description"))
                    .displayOrder(((Number) m.getOrDefault("displayOrder", 0)).intValue())
                    .build());
        }
        topicRepository.saveAll(topics);
        log.info("Seeded {} topics", topics.size());
    }

    private void seedTags() {
        /*if (tagRepository.count() > 0) {
            log.info("Tags already seeded, skipping.");
            return;
        }*/
        Optional<List<Map<String, Object>>> raw = readJsonArray("seed-data/seed-tags.json");
        if (raw.isEmpty()) return;

        List<Tag> tags = new ArrayList<>();
        for (Map<String, Object> m : raw.get()) {
            tags.add(Tag.builder().name((String) m.get("name")).usageCount(0).build());
        }
        tagRepository.saveAll(tags);
        log.info("Seeded {} tags", tags.size());
    }

    private void seedCompanies() {
        if (companyRepository.count() > 0) {
            log.info("Companies already seeded, skipping.");
            return;
        }
        Optional<List<Map<String, Object>>> raw = readJsonArray("seed-data/seed-companies.json");
        if (raw.isEmpty() || raw.get().isEmpty()) return; // source data currently has 0 companies

        List<Company> companies = new ArrayList<>();
        for (Map<String, Object> m : raw.get()) {
            companies.add(Company.builder()
                    .name((String) m.get("name"))
                    .logoUrl((String) m.get("logoUrl"))
                    .usageCount(0)
                    .build());
        }
        companyRepository.saveAll(companies);
        log.info("Seeded {} companies", companies.size());
    }

    @SuppressWarnings("unchecked")
    private void seedQuestions() {
        /*if (questionRepository.count() > 0) {
            log.info("Questions already seeded, skipping.");
            return;
        }*/
        Optional<List<Map<String, Object>>> rawOpt = readJsonArray("seed-data/seed-questions.json");
        if (rawOpt.isEmpty()) return;
        List<Map<String, Object>> raw = rawOpt.get();

        List<Question> questions = new ArrayList<>();

        for (Map<String, Object> m : raw) {
            Question q = Question.builder()
                    .questionType(QuestionType.valueOf((String) m.get("questionType")))
                    .status(QuestionStatus.valueOf((String) m.get("status")))
                    .title((String) m.get("title"))
                    .topic((String) m.get("topic"))
                    .category((String) m.get("category"))
                    .priority((String) m.get("priority"))
                    .difficulty((String) m.get("difficulty"))
                    .tags((List<String>) m.getOrDefault("tags", List.of()))
                    .companyAskedIn((List<String>) m.getOrDefault("companyAskedIn", List.of()))
                    .shortSummary((String) m.get("shortSummary"))
                    .viewCount(0)
                    .bookmarkCount(0)
                    .usefulVoteCount(0)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    // theory
                    .answer((String) m.get("answer"))
                    .deepExplanation((String) m.get("deepExplanation"))
                    .followup((String) m.get("followup"))
                    .realWorldUsage((String) m.get("realWorldUsage"))
                    // dsa
                    .week((String) m.get("week"))
                    .timeEstimate((String) m.get("timeEstimate"))
                    .intro((String) m.get("intro"))
                    .intuition((String) m.get("intuition"))
                    .approach((String) m.get("approach"))
                    .example((String) m.get("example"))
                    .code((String) m.get("code"))
                    .timeComplexity((String) m.get("timeComplexity"))
                    .spaceComplexity((String) m.get("spaceComplexity"))
                    .edgeCases((List<String>) m.get("edgeCases"))
                    .talkTrack((String) m.get("talkTrack"))
                    .followups((List<String>) m.get("followups"))
                    // system design
                    .problemStatement((String) m.get("problemStatement"))
                    .requirements((String) m.get("requirements"))
                    .functionalRequirements((String) m.get("functionalRequirements"))
                    .nonFunctionalRequirements((String) m.get("nonFunctionalRequirements"))
                    .highLevelDesign((String) m.get("highLevelDesign"))
                    .apiDesign((String) m.get("apiDesign"))
                    .dbDesign((String) m.get("dbDesign"))
                    .scalingStrategy((String) m.get("scalingStrategy"))
                    .cachingStrategy((String) m.get("cachingStrategy"))
                    .consistencyTradeoffs((String) m.get("consistencyTradeoffs"))
                    .failureScenarios((String) m.get("failureScenarios"))
                    .observability((String) m.get("observability"))
                    .security((String) m.get("security"))
                    .tradeoffs((String) m.get("tradeoffs"))
                    .build();
            questions.add(q);
        }

        questionRepository.saveAll(questions);
        log.info("Seeded {} questions ({} theory, {} dsa, {} system design)", questions.size(),
                questions.stream().filter(q -> q.getQuestionType() == QuestionType.THEORY).count(),
                questions.stream().filter(q -> q.getQuestionType() == QuestionType.DSA).count(),
                questions.stream().filter(q -> q.getQuestionType() == QuestionType.SYSTEM_DESIGN).count());
    }

    private void seedReferenceContent() {
        if (referenceContentRepository.count() > 0) {
            log.info("Reference content already seeded, skipping.");
            return;
        }
        Optional<List<Map<String, Object>>> raw = readJsonArray("seed-data/seed-reference-content.json");
        if (raw.isEmpty()) return;

        List<ReferenceContent> pages = new ArrayList<>();
        int order = 0;
        for (Map<String, Object> m : raw.get()) {
            pages.add(ReferenceContent.builder()
                    .pageKey((String) m.get("pageKey"))
                    .icon((String) m.get("icon"))
                    .title((String) m.get("title"))
                    .description((String) m.get("description"))
                    .bodyHtml((String) m.get("bodyHtml"))
                    .displayOrder(m.containsKey("displayOrder")
                            ? ((Number) m.get("displayOrder")).intValue()
                            : order)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());
            order++;
        }
        referenceContentRepository.saveAll(pages);
        log.info("Seeded {} reference content pages", pages.size());
    }

    private void seedCheatSheetItems() {
        if (cheatSheetItemRepository.count() > 0) {
            log.info("Cheat sheet items already seeded, skipping.");
            return;
        }
        Optional<List<Map<String, Object>>> raw = readJsonArray("seed-data/seed-cheatsheet.json");
        if (raw.isEmpty()) return;

        List<CheatSheetItem> items = new ArrayList<>();
        for (Map<String, Object> m : raw.get()) {
            items.add(CheatSheetItem.builder()
                    .category((String) m.get("category"))
                    .categoryLabel((String) m.get("categoryLabel"))
                    .categoryIcon((String) m.get("categoryIcon"))
                    .question((String) m.get("question"))
                    .answer((String) m.get("answer"))
                    .displayOrder(((Number) m.getOrDefault("displayOrder", 0)).intValue())
                    .build());
        }
        cheatSheetItemRepository.saveAll(items);
        log.info("Seeded {} cheat sheet items", items.size());
    }

    private void seedAdminUser() {
        String email = adminBootstrapProperties.getBootstrapEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (userRepository.existsByEmail(email.toLowerCase())) {
            log.info("Bootstrap admin already exists, skipping.");
            return;
        }
        User admin = User.builder()
                .name("Admin")
                .email(email.toLowerCase())
                .passwordHash(passwordEncoder.encode(adminBootstrapProperties.getBootstrapPassword()))
                .roles(new HashSet<>(Set.of(Role.USER, Role.ADMIN)))
                .enabled(true)
                .createdAt(Instant.now())
                .build();
        userRepository.save(admin);
        log.info("Bootstrap admin user created: {}", email);
    }

    /**
     * Reads a JSON array from the classpath, if present. Returns Optional.empty()
     * (logging a single info line) when the resource doesn't exist, rather than
     * throwing — this is what keeps the application bootable when seed files are
     * intentionally not bundled. Any other IO/parse failure IS rethrown as an
     * unchecked exception, since that indicates a genuinely malformed seed file
     * that was bundled, which should be visible rather than silently skipped.
     */
    @SuppressWarnings("unchecked")
    private Optional<List<Map<String, Object>>> readJsonArray(String classpathLocation) {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        if (!resource.exists()) {
            log.info("Seed file '{}' not found on classpath, skipping this step.", classpathLocation);
            return Optional.empty();
        }
        try (InputStream is = resource.getInputStream()) {
            return Optional.of(objectMapper.readValue(is, List.class));
        } catch (FileNotFoundException e) {
            log.info("Seed file '{}' not found on classpath, skipping this step.", classpathLocation);
            return Optional.empty();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read/parse seed file: " + classpathLocation, e);
        }
    }
}
