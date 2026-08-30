package com.calculon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "achievements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // e.g. "STREAK_7", "FIRST_LESSON", "CALCULUS_MASTER"

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String icon;

    @Enumerated(EnumType.STRING)
    private TriggerType triggerType;

    /** Threshold value used to evaluate the trigger, e.g. 7 for a 7-day streak. */
    private Integer triggerValue;

    private Integer xpBonus = 0;

    public enum TriggerType {
        STREAK_DAYS, TOTAL_XP, LESSONS_COMPLETED, DOMAIN_MASTERED, LEVEL_REACHED, QUESTIONS_CORRECT
    }
}
