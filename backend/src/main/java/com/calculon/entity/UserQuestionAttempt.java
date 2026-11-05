package com.calculon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One row per user per question: how many times they have tried it and whether they have ever answered it
 * correctly. QuestionService uses it so XP for a question is paid out once, the first time it is answered
 * correctly, and repeating the same question cannot be used to farm XP.
 */
@Entity
@Table(name = "user_question_attempts",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_id"}))
@Getter
@Setter
@NoArgsConstructor
public class UserQuestionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /** How many times the user has submitted an answer to this question. */
    private Integer attempts = 0;

    /** True once the user has answered this question correctly at least once (XP already paid). */
    private boolean correct = false;

    private Integer xpEarned = 0;

    private LocalDateTime firstCorrectAt;
}
