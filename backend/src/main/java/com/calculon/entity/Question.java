package com.calculon.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, length = 2000)
    private String prompt;

    @Enumerated(EnumType.STRING)
    private QuestionType type = QuestionType.MULTIPLE_CHOICE;

    /** For MULTIPLE_CHOICE: list of options. For NUMERIC/EXPRESSION: usually empty. */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<QuestionOption> options = new ArrayList<>();

    /** Correct answer for NUMERIC/EXPRESSION questions (e.g. "4" or "x^2+1"). Ignored for MULTIPLE_CHOICE. */
    private String correctAnswer;

    /** Explanation shown after the user answers, right or wrong. */
    @Column(length = 2000)
    private String explanation;

    @Enumerated(EnumType.STRING)
    private Lesson.Difficulty difficulty = Lesson.Difficulty.BEGINNER;

    /** XP awarded for a correct answer. */
    private Integer xpReward = 10;

    public enum QuestionType {
        MULTIPLE_CHOICE, NUMERIC, EXPRESSION
    }
}
