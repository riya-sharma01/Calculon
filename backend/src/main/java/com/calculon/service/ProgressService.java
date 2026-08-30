package com.calculon.service;

import com.calculon.dto.ProgressDtos;
import com.calculon.entity.*;
import com.calculon.repository.LessonRepository;
import com.calculon.repository.UserAchievementRepository;
import com.calculon.repository.UserLessonProgressRepository;
import com.calculon.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProgressService {

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final UserLessonProgressRepository progressRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final GamificationService gamificationService;

    public ProgressService(UserRepository userRepository,
                            LessonRepository lessonRepository,
                            UserLessonProgressRepository progressRepository,
                            UserAchievementRepository userAchievementRepository,
                            GamificationService gamificationService) {
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.progressRepository = progressRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.gamificationService = gamificationService;
    }

    /** Marks a lesson as (further) attempted or completed, and awards lesson-completion XP once per lesson. */
    public ProgressDtos.LevelUpEvent completeLesson(User user, Long lessonId, Integer scorePercent) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        UserLessonProgress progress = progressRepository.findByUserIdAndLessonId(user.getId(), lessonId)
                .orElseGet(() -> {
                    UserLessonProgress p = new UserLessonProgress();
                    p.setUser(user);
                    p.setLesson(lesson);
                    p.setStartedAt(LocalDateTime.now());
                    return p;
                });

        boolean firstCompletion = progress.getStatus() != UserLessonProgress.Status.COMPLETED
                && scorePercent != null && scorePercent >= 70;

        progress.setAttempts(progress.getAttempts() + 1);
        if (scorePercent != null && scorePercent > progress.getBestScorePercent()) {
            progress.setBestScorePercent(scorePercent);
        }
        if (firstCompletion) {
            progress.setStatus(UserLessonProgress.Status.COMPLETED);
            progress.setCompletedAt(LocalDateTime.now());
        } else if (progress.getStatus() == UserLessonProgress.Status.NOT_STARTED) {
            progress.setStatus(UserLessonProgress.Status.IN_PROGRESS);
        }
        progressRepository.save(progress);

        GamificationService.XpResult result = new GamificationService.XpResult();
        if (firstCompletion) {
            result = gamificationService.awardXp(user, lesson.getBaseXpReward());
        }

        return new ProgressDtos.LevelUpEvent(
                result.leveledUp,
                result.newLevel,
                result.newAchievements.stream().map(Achievement::getName).toList()
        );
    }

    public ProgressDtos.DashboardResponse dashboard(User user) {
        long lessonsCompleted = progressRepository.countByUserIdAndStatus(user.getId(), UserLessonProgress.Status.COMPLETED);
        List<String> recentAchievements = userAchievementRepository.findByUserId(user.getId()).stream()
                .sorted((a, b) -> b.getEarnedAt().compareTo(a.getEarnedAt()))
                .limit(5)
                .map(ua -> ua.getAchievement().getName())
                .toList();

        return new ProgressDtos.DashboardResponse(
                user.getId(),
                user.getTotalXp(),
                user.getLevel(),
                gamificationService.xpIntoCurrentLevel(user.getTotalXp()),
                gamificationService.xpForLevel(user.getLevel()),
                user.getCurrentStreak(),
                user.getLongestStreak(),
                lessonsCompleted,
                recentAchievements
        );
    }
}
