package com.calculon.service;

import com.calculon.dto.QuestionDtos;
import com.calculon.entity.Question;
import com.calculon.entity.QuestionOption;
import com.calculon.entity.User;
import com.calculon.repository.QuestionRepository;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final GamificationService gamificationService;

    public QuestionService(QuestionRepository questionRepository, GamificationService gamificationService) {
        this.questionRepository = questionRepository;
        this.gamificationService = gamificationService;
    }

    /** Grades an answer, awards XP through GamificationService if correct, and returns the verdict. */
    public QuestionDtos.AnswerResult submitAnswer(User user, QuestionDtos.AnswerRequest request) {
        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        boolean correct = switch (question.getType()) {
            case MULTIPLE_CHOICE -> question.getOptions().stream()
                    .filter(QuestionOption::isCorrect)
                    .anyMatch(o -> o.getId().equals(request.selectedOptionId()));
            case NUMERIC, EXPRESSION -> question.getCorrectAnswer() != null
                    && normalize(question.getCorrectAnswer()).equals(normalize(request.freeResponse()));
        };

        Integer xpAwarded = null;
        if (correct) {
            xpAwarded = question.getXpReward();
            gamificationService.awardXp(user, xpAwarded);
        }

        return new QuestionDtos.AnswerResult(correct, question.getExplanation(), xpAwarded);
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase().replaceAll("\\s+", "");
    }
}
