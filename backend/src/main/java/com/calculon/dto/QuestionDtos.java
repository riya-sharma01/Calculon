package com.calculon.dto;

import com.calculon.entity.Question;

import java.util.List;

public class QuestionDtos {

    /** Shape returned to the client BEFORE answering — never exposes which option is correct. */
    public record QuestionPublic(
            Long id,
            String prompt,
            Question.QuestionType type,
            List<OptionPublic> options
    ) {}

    public record OptionPublic(Long id, String text) {}

    public record AnswerRequest(
            Long questionId,
            Long selectedOptionId, // for MULTIPLE_CHOICE
            String freeResponse    // for NUMERIC / EXPRESSION
    ) {}

    public record AnswerResult(
            boolean correct,
            String correctAnswerExplanation,
            Integer xpAwarded
    ) {}
}
