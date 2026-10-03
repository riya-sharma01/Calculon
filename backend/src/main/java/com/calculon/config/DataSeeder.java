package com.calculon.config;

import com.calculon.entity.Achievement;
import com.calculon.repository.AchievementRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeds gamification achievements on first run.
 *
 * Domains, lessons, and questions are no longer hardcoded here — they are loaded from the
 * JSON question bank (src/main/resources/questions/*.json) by {@link JsonQuestionSeeder}, which
 * runs after this seeder. See JsonQuestionSeeder for how that content is kept in sync on re-deploy.
 */
@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final AchievementRepository achievementRepository;

    public DataSeeder(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    @Override
    public void run(String... args) {
        if (achievementRepository.count() > 0) return;
        seedAchievements();
    }

    private void seedAchievements() {
        achievement("FIRST_LESSON", "First Steps", "Complete your first lesson.", Achievement.TriggerType.LESSONS_COMPLETED, 1, 20);
        achievement("LESSONS_5", "Getting Somewhere", "Complete 5 lessons.", Achievement.TriggerType.LESSONS_COMPLETED, 5, 40);
        achievement("STREAK_3", "Warming Up", "Reach a 3-day learning streak.", Achievement.TriggerType.STREAK_DAYS, 3, 30);
        achievement("STREAK_7", "On a Roll", "Reach a 7-day learning streak.", Achievement.TriggerType.STREAK_DAYS, 7, 75);
        achievement("LEVEL_5", "Rising Mathematician", "Reach level 5.", Achievement.TriggerType.LEVEL_REACHED, 5, 50);
        achievement("XP_1000", "Four Digits", "Earn 1000 total XP.", Achievement.TriggerType.TOTAL_XP, 1000, 100);
    }

    private void achievement(String code, String name, String desc, Achievement.TriggerType type, int value, int xpBonus) {
        Achievement a = new Achievement();
        a.setCode(code);
        a.setName(name);
        a.setDescription(desc);
        a.setTriggerType(type);
        a.setTriggerValue(value);
        a.setXpBonus(xpBonus);
        achievementRepository.save(a);
    }
}
