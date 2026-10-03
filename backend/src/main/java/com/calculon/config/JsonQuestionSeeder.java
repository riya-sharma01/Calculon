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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

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

    public JsonQuestionSeeder(MathDomainRepository domainRepository, LessonRepository lessonRepository,
                               QuestionRepository questionRepository) {
        this.domainRepository = domainRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] files = resolver.getResources("classpath:questions/*.json");

        if (files.length == 0) {
            log.warn("No question-bank JSON files found under classpath:questions/ — skipping JSON seeding.");
            return;
        }

        int totalInserted = 0;
        int totalSkipped = 0;

        for (Resource file : files) {
            try (InputStream in = file.getInputStream()) {
                QuestionBankFile bank = objectMapper.readValue(in, QuestionBankFile.class);
                int[] counts = loadBank(bank);
                totalInserted += counts[0];
                totalSkipped += counts[1];
                log.info("Question bank '{}': {} inserted, {} already present.", bank.getDomain(), counts[0], counts[1]);
            } catch (IOException e) {
                log.error("Failed to load question bank file {}: {}", file.getFilename(), e.getMessage(), e);
            }
        }

        log.info("JsonQuestionSeeder finished: {} questions inserted, {} already present across {} file(s).",
                totalInserted, totalSkipped, files.length);
    }

    /** @return {insertedCount, skippedCount} */
    private int[] loadBank(QuestionBankFile bank) {
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

        for (QuestionBankFile.LevelBlock levelBlock : bank.getLevels()) {
            Lesson lesson = findOrCreateLesson(domain, levelBlock);

            if (levelBlock.getQuestions() == null) continue;

            for (QuestionBankFile.QuestionBlock qb : levelBlock.getQuestions()) {
                if (qb.getId() == null) {
                    log.warn("Skipping a question with no id in domain '{}', level '{}'.", bank.getDomain(), levelBlock.getLevelName());
                    continue;
                }
                if (questionRepository.existsByExternalId(qb.getId())) {
                    skipped++;
                    continue;
                }
                questionRepository.save(toQuestion(qb, lesson));
                inserted++;
            }
        }

        return new int[]{inserted, skipped};
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
