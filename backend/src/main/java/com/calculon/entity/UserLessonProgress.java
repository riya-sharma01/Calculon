package com.calculon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_lesson_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "lesson_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    private Status status = Status.NOT_STARTED;

    /** Best percentage score achieved (0-100) across attempts of this lesson's questions. */
    private Integer bestScorePercent = 0;

    private Integer attempts = 0;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public enum Status {
        NOT_STARTED, IN_PROGRESS, COMPLETED
    }
}
