package com.calculon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A top-level area of mathematics the platform teaches
 * (e.g. Calculus, Trigonometry, Linear Algebra, Probability, Number Theory).
 */
@Entity
@Table(name = "math_domains")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MathDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(unique = true)
    private String slug;

    @Column(length = 1000)
    private String description;

    /** Name of the icon/visualization type used on the frontend, e.g. "function-grapher". */
    private String visualizationType;

    /** Order in which domains are displayed. */
    private Integer displayOrder = 0;
}
