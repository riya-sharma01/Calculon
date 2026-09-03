package com.calculon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    private MathDomain domain;

    @Column(nullable = false)
    private String title;

    @Column(length = 4000)
    private String summary;

    /** Markdown/HTML body content for the lesson explanation. */
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty = Difficulty.BEGINNER;

    /** Order within the domain's learning path. */
    private Integer sequenceOrder = 0;

    /** Base XP awarded for completing this lesson (before streak/difficulty bonuses). */
    private Integer baseXpReward = 50;

    public enum Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }
}
