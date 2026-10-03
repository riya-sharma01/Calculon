package com.calculon.dto;

import com.calculon.entity.Lesson;

import java.util.List;

public class LessonDtos {

    public record LessonSummary(
            Long id,
            String title,
            String summary,
            Lesson.Difficulty difficulty,
            Integer sequenceOrder,
            Integer baseXpReward,
            String domainName,
            String progressStatus // null if not authenticated / not started
    ) {}

    public record LessonDetail(
            Long id,
            String title,
            String summary,
            String content,
            Lesson.Difficulty difficulty,
            Integer baseXpReward,
            String domainName,
            List<QuestionDtos.QuestionPublic> questions
    ) {}
}
