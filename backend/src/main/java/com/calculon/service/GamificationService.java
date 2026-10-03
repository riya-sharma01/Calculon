package com.calculon.service;

import com.calculon.entity.Achievement;
import com.calculon.entity.User;
import com.calculon.entity.UserAchievement;
import com.calculon.entity.UserLessonProgress;
import com.calculon.repository.AchievementRepository;
import com.calculon.repository.UserAchievementRepository;
import com.calculon.repository.UserLessonProgressRepository;
import com.calculon.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Central home for XP, levels, streaks, and achievement unlocking.
 * Every other service should route gamification changes through here so the
 * rules stay in one place.
 */
@Service
public class GamificationService {

    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserLessonProgressRepository progressRepository;

    public GamificationService(UserRepository userRepository,
                                AchievementRepository achievementRepository,
                                UserAchievementRepository userAchievementRepository,
                                UserLessonProgressRepository progressRepository) {
        this.userRepository = userRepository;
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.progressRepository = progressRepository;
    }

    /** XP required to go from level N to level N+1: a gentle upward curve. */
    public int xpForLevel(int level) {
        return 100 + (level - 1) * 50;
    }

    /** Recomputes level from totalXp using xpForLevel thresholds. */
    public int levelForTotalXp(long totalXp) {
        int level = 1;
        long remaining = totalXp;
        while (remaining >= xpForLevel(level)) {
            remaining -= xpForLevel(level);
            level++;
        }
        return level;
    }

    public int xpIntoCurrentLevel(long totalXp) {
        int level = 1;
        long remaining = totalXp;
        while (remaining >= xpForLevel(level)) {
            remaining -= xpForLevel(level);
            level++;
        }
        return (int) remaining;
    }

    public static class XpResult {
        public boolean leveledUp;
        public int newLevel;
        public List<Achievement> newAchievements = new ArrayList<>();
    }

    /**
     * Awards XP to a user, recomputes their level, updates their daily streak,
     * and unlocks any achievements they've now earned.
     */
    public XpResult awardXp(User user, int xp) {
        XpResult result = new XpResult();

        int previousLevel = user.getLevel();
        user.setTotalXp(user.getTotalXp() + xp);
        int newLevel = levelForTotalXp(user.getTotalXp());
        user.setLevel(newLevel);
        result.leveledUp = newLevel > previousLevel;
        result.newLevel = newLevel;

        updateStreak(user);
        userRepository.save(user);

        result.newAchievements.addAll(evaluateAchievements(user));
        return result;
    }

    /** Bumps the streak counter based on whether the user was active yesterday, today already, or lapsed. */
    private void updateStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate last = user.getLastActivityDate();

        if (last == null || last.isBefore(today.minusDays(1))) {
            user.setCurrentStreak(1); // lapsed or first-ever activity
        } else if (last.equals(today.minusDays(1))) {
            user.setCurrentStreak(user.getCurrentStreak() + 1); // consecutive day
        }
        // if last.equals(today), streak already counted today — no change

        user.setLastActivityDate(today);
        if (user.getCurrentStreak() > user.getLongestStreak()) {
            user.setLongestStreak(user.getCurrentStreak());
        }
    }

    private List<Achievement> evaluateAchievements(User user) {
        List<Achievement> unlocked = new ArrayList<>();
        long lessonsCompleted = progressRepository.countByUserIdAndStatus(
                user.getId(), UserLessonProgress.Status.COMPLETED);

        for (Achievement achievement : achievementRepository.findAll()) {
            if (userAchievementRepository.existsByUserIdAndAchievementId(user.getId(), achievement.getId())) {
                continue;
            }
            boolean earned = switch (achievement.getTriggerType()) {
                case STREAK_DAYS -> user.getCurrentStreak() >= achievement.getTriggerValue();
                case TOTAL_XP -> user.getTotalXp() >= achievement.getTriggerValue();
                case LESSONS_COMPLETED -> lessonsCompleted >= achievement.getTriggerValue();
                case LEVEL_REACHED -> user.getLevel() >= achievement.getTriggerValue();
                case DOMAIN_MASTERED, QUESTIONS_CORRECT -> false; // evaluated elsewhere with extra context
            };
            if (earned) {
                UserAchievement ua = new UserAchievement();
                ua.setUser(user);
                ua.setAchievement(achievement);
                userAchievementRepository.save(ua);
                if (achievement.getXpBonus() != null && achievement.getXpBonus() > 0) {
                    user.setTotalXp(user.getTotalXp() + achievement.getXpBonus());
                    userRepository.save(user);
                }
                unlocked.add(achievement);
            }
        }
        return unlocked;
    }
}
