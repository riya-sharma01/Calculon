package com.calculon.dto;

import java.util.List;

public class ProgressDtos {

    public record DashboardResponse(
            Long userId,
            Long totalXp,
            Integer level,
            Integer xpIntoCurrentLevel,
            Integer xpNeededForNextLevel,
            Integer currentStreak,
            Integer longestStreak,
            long lessonsCompleted,
            List<String> recentAchievements
    ) {}

    public record LessonCompletionRequest(
            Long lessonId,
            Integer scorePercent
    ) {}

    public record LevelUpEvent(
            boolean leveledUp,
            Integer newLevel,
            List<String> newAchievements
    ) {}
}
