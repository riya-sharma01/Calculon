package com.calculon.config;

import com.calculon.dto.QuestionBankFile;
import com.calculon.entity.Lesson;
import com.calculon.entity.MathDomain;
import com.calculon.entity.Question;
import com.calculon.entity.QuestionOption;
import com.calculon.repository.LessonRepository;
import com.calculon.repository.MathDomainRepository;
import com.calculon.repository.QuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Loads every domain's question bank from classpath:questions/*.json into the database on boot.
 *
 * This is the sole source of {@link MathDomain}, {@link Lesson}, and {@link Question} rows — see
 * {@link DataSeeder} for why the old hardcoded content was removed. Each question's "id" field from
 * the JSON (e.g. "alg3-h-bt-03") is stored as {@link Question#getExternalId()} and checked before
 * inserting, so re-deploying with an updated or expanded JSON file (more questions added to a domain,
 * a wording fix, etc.) only inserts what's new — it never wipes or duplicates what's already loaded.
 *
 * Domains and lessons are matched the same idempotent way: a domain is found by name (or created), and
 * within it a lesson is found by its slug "<domain-slug>-<level-name>" (or created) to hold that
 * level's questions. Dropping a fresh questions/*.json file into resources and redeploying is therefore
 * always safe to repeat.
 */
@Component
@Order(2)
public class JsonQuestionSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(JsonQuestionSeeder.class);

    private final MathDomainRepository domainRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TransactionTemplate transactionTemplate;

    public JsonQuestionSeeder(MathDomainRepository domainRepository, LessonRepository lessonRepository,
                               QuestionRepository questionRepository, PlatformTransactionManager transactionManager) {
        this.domainRepository = domainRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * Each file is loaded in its own transaction (not one giant transaction for everything), and a failure
     * in one file is logged and skipped rather than thrown. A CommandLineRunner that throws takes the whole
     * application down at startup, which on a host like Render shows up as an endless crash-and-restart loop.
     */
    @Override
    public void run(String... args) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] files = resolver.getResources("classpath:questions/*.json");

        if (files.length == 0) {
            log.warn("No question-bank JSON files found under classpath:questions/ — skipping JSON seeding.");
            return;
        }

        int totalInserted = 0;
        int totalSkipped = 0;

        // One query up front instead of one existsByExternalId() call per question (932 round trips to the
        // remote database on every restart, which made cold starts much slower).
        Set<String> knownIds = new HashSet<>(questionRepository.findAllExternalIds());

        for (Resource file : files) {
            try (InputStream in = file.getInputStream()) {
                QuestionBankFile bank = objectMapper.readValue(in, QuestionBankFile.class);
                int[] counts = transactionTemplate.execute(status -> loadBank(bank, knownIds));
                if (counts == null) {
                    counts = new int[]{0, 0};
                }
                totalInserted += counts[0];
                totalSkipped += counts[1];
                log.info("Question bank '{}': {} inserted, {} already present.", bank.getDomain(), counts[0], counts[1]);
            } catch (IOException | RuntimeException e) {
                log.error("Failed to load question bank file {}: {}", file.getFilename(), e.getMessage(), e);
            }
        }

        log.info("JsonQuestionSeeder finished: {} questions inserted, {} already present across {} file(s).",
                totalInserted, totalSkipped, files.length);
    }

    /** @return {insertedCount, skippedCount} */
    private int[] loadBank(QuestionBankFile bank, Set<String> knownIds) {
        if (bank.getDomain() == null || bank.getLevels() == null) {
            log.warn("Skipping malformed question bank file (missing domain or levels).");
            return new int[]{0, 0};
        }

        MathDomain domain = domainRepository.findByName(bank.getDomain()).orElseGet(() -> {
            MathDomain d = new MathDomain();
            d.setName(bank.getDomain());
            d.setSlug(slugify(bank.getDomain()));
            d.setDescription(bank.getDomain() + " question bank.");
            d.setVisualizationType("function-grapher");
            d.setDisplayOrder(0);
            return domainRepository.save(d);
        });

        int inserted = 0;
        int skipped = 0;
        int updated = 0;

        for (QuestionBankFile.LevelBlock levelBlock : bank.getLevels()) {
            Lesson lesson = findOrCreateLesson(domain, levelBlock);

            if (levelBlock.getQuestions() == null) continue;

            // The lesson's existing questions, loaded once, so edits made in the JSON can be applied below.
            java.util.Map<String, Question> existingInLesson = new java.util.HashMap<>();
            for (Question existing : questionRepository.findByLessonIdWithOptions(lesson.getId())) {
                if (existing.getExternalId() != null) existingInLesson.put(existing.getExternalId(), existing);
            }

            for (QuestionBankFile.QuestionBlock qb : levelBlock.getQuestions()) {
                if (qb.getId() == null) {
                    log.warn("Skipping a question with no id in domain '{}', level '{}'.", bank.getDomain(), levelBlock.getLevelName());
                    continue;
                }
                if (!knownIds.add(qb.getId())) {
                    // Already in the database. If the JSON text changed since it was loaded, bring it up to date.
                    Question current = existingInLesson.get(qb.getId());
                    if (current != null && refreshIfChanged(current, toQuestion(qb, lesson))) {
                        questionRepository.save(current);
                        updated++;
                    }
                    skipped++;
                    continue;
                }
                questionRepository.save(toQuestion(qb, lesson));
                inserted++;
            }
        }
        if (updated > 0) {
            log.info("Question bank '{}': {} existing question(s) updated from the JSON.", bank.getDomain(), updated);
        }

        return new int[]{inserted, skipped};
    }

    /**
     * Copies wording, answer, explanation, XP and options from the JSON version onto the stored question when
     * any of them differ. The id, lesson and user history are untouched, so editing a question in the JSON and
     * redeploying fixes it on the live site without wiping anything.
     * @return true if the stored question was changed
     */
    private boolean refreshIfChanged(Question stored, Question fresh) {
        boolean sameOptions = stored.getOptions().size() == fresh.getOptions().size();
        for (int i = 0; sameOptions && i < fresh.getOptions().size(); i++) {
            QuestionOption a = stored.getOptions().get(i), b = fresh.getOptions().get(i);
            sameOptions = java.util.Objects.equals(a.getText(), b.getText()) && a.isCorrect() == b.isCorrect();
        }
        boolean same = sameOptions
                && java.util.Objects.equals(stored.getPrompt(), fresh.getPrompt())
                && stored.getType() == fresh.getType()
                && java.util.Objects.equals(stored.getTopic(), fresh.getTopic())
                && java.util.Objects.equals(stored.getExplanation(), fresh.getExplanation())
                && java.util.Objects.equals(stored.getCorrectAnswer(), fresh.getCorrectAnswer())
                && java.util.Objects.equals(stored.getXpReward(), fresh.getXpReward());
        if (same) return false;

        stored.setPrompt(fresh.getPrompt());
        stored.setType(fresh.getType());
        stored.setTopic(fresh.getTopic());
        stored.setExplanation(fresh.getExplanation());
        stored.setCorrectAnswer(fresh.getCorrectAnswer());
        stored.setXpReward(fresh.getXpReward());
        if (!sameOptions) {
            stored.getOptions().clear();
            for (QuestionOption opt : fresh.getOptions()) {
                opt.setQuestion(stored);
                stored.getOptions().add(opt);
            }
        }
        return true;
    }

    private Lesson findOrCreateLesson(MathDomain domain, QuestionBankFile.LevelBlock levelBlock) {
        String lessonSlug = domain.getSlug() + "-" + slugify(levelBlock.getLevelName());

        return lessonRepository.findBySlug(lessonSlug).orElseGet(() -> {
            Lesson l = new Lesson();
            l.setDomain(domain);
            l.setSlug(lessonSlug);
            l.setTitle(levelBlock.getLevelName() + " — " + domain.getName());
            l.setSummary(levelBlock.getLevelName() + " level practice questions for " + domain.getName() + ".");
            l.setDifficulty(mapDifficulty(levelBlock.getLevelName()));
            l.setSequenceOrder(levelBlock.getLevel());
            l.setBaseXpReward(50);
            return lessonRepository.save(l);
        });
    }

    private Question toQuestion(QuestionBankFile.QuestionBlock qb, Lesson lesson) {
        Question q = new Question();
        q.setExternalId(qb.getId());
        q.setTopic(qb.getTopic());
        q.setLesson(lesson);
        q.setPrompt(qb.getPrompt());
        q.setType(mapType(qb.getType()));
        q.setExplanation(qb.getExplanation());
        q.setDifficulty(lesson.getDifficulty());
        q.setXpReward(qb.getXp() > 0 ? qb.getXp() : 10);

        if (q.getType() == Question.QuestionType.MULTIPLE_CHOICE && qb.getOptions() != null) {
            for (QuestionBankFile.OptionBlock ob : qb.getOptions()) {
                QuestionOption opt = new QuestionOption();
                opt.setQuestion(q);
                opt.setText(ob.getText());
                opt.setCorrect(ob.isCorrect());
                q.getOptions().add(opt);
            }
        } else {
            q.setCorrectAnswer(qb.getAnswer());
        }

        return q;
    }

    private Question.QuestionType mapType(String rawType) {
        if (rawType == null) return Question.QuestionType.MULTIPLE_CHOICE;
        try {
            return Question.QuestionType.valueOf(rawType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown question type '{}', defaulting to MULTIPLE_CHOICE.", rawType);
            return Question.QuestionType.MULTIPLE_CHOICE;
        }
    }

    private Lesson.Difficulty mapDifficulty(String levelName) {
        if (levelName == null) return Lesson.Difficulty.FOUNDATION;
        try {
            return Lesson.Difficulty.valueOf(levelName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown level name '{}', defaulting to FOUNDATION.", levelName);
            return Lesson.Difficulty.FOUNDATION;
        }
    }

    private String slugify(String s) {
        return s.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }
}
