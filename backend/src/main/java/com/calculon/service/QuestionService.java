package com.calculon.service;

import com.calculon.dto.QuestionDtos;
import com.calculon.entity.Question;
import com.calculon.entity.QuestionOption;
import com.calculon.entity.User;
import com.calculon.entity.UserQuestionAttempt;
import com.calculon.repository.QuestionRepository;
import com.calculon.repository.UserQuestionAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final GamificationService gamificationService;
    private final UserQuestionAttemptRepository attemptRepository;

    public QuestionService(QuestionRepository questionRepository,
                           GamificationService gamificationService,
                           UserQuestionAttemptRepository attemptRepository) {
        this.questionRepository = questionRepository;
        this.gamificationService = gamificationService;
        this.attemptRepository = attemptRepository;
    }

    /** Grades an answer, awards XP through GamificationService if correct, and returns the verdict. */
    @Transactional
    public QuestionDtos.AnswerResult submitAnswer(User user, QuestionDtos.AnswerRequest request) {
        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        boolean correct = switch (question.getType()) {
            case MULTIPLE_CHOICE -> question.getOptions().stream()
                    .filter(QuestionOption::isCorrect)
                    .anyMatch(o -> o.getId().equals(request.selectedOptionId()));
            // Plain text match first (cheap), then AnswerMatcher, which accepts equivalent forms such as
            // 1/2 = 0.5, sqrt3 = √3, x^2+1 = 1+x^2, or an equation rearranged.
            case NUMERIC, EXPRESSION -> question.getCorrectAnswer() != null
                    && (normalize(question.getCorrectAnswer()).equals(normalize(request.freeResponse()))
                        || AnswerMatcher.matches(question.getCorrectAnswer(), request.freeResponse()));
        };

        // XP for a question is paid once: the first time this user answers it correctly.
        UserQuestionAttempt attempt = attemptRepository
                .findByUserIdAndQuestionId(user.getId(), question.getId())
                .orElseGet(() -> {
                    UserQuestionAttempt a = new UserQuestionAttempt();
                    a.setUser(user);
                    a.setQuestion(question);
                    return a;
                });
        attempt.setAttempts(attempt.getAttempts() + 1);
        boolean alreadyEarned = attempt.isCorrect();

        Integer xpAwarded = null;
        if (correct && !alreadyEarned) {
            xpAwarded = question.getXpReward();
            gamificationService.awardXp(user, xpAwarded);
            attempt.setCorrect(true);
            attempt.setXpEarned(xpAwarded);
            attempt.setFirstCorrectAt(LocalDateTime.now());
        }
        attemptRepository.save(attempt);

        return new QuestionDtos.AnswerResult(correct, question.getExplanation(), xpAwarded, correct && alreadyEarned);
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase().replaceAll("\\s+", "");
    }
}
