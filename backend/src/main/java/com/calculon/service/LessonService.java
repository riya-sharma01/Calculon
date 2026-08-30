package com.calculon.service;

import com.calculon.dto.LessonDtos;
import com.calculon.dto.QuestionDtos;
import com.calculon.entity.Lesson;
import com.calculon.entity.Question;
import com.calculon.entity.UserLessonProgress;
import com.calculon.repository.LessonRepository;
import com.calculon.repository.QuestionRepository;
import com.calculon.repository.UserLessonProgressRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final UserLessonProgressRepository progressRepository;

    public LessonService(LessonRepository lessonRepository,
                          QuestionRepository questionRepository,
                          UserLessonProgressRepository progressRepository) {
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
        this.progressRepository = progressRepository;
    }

    public List<LessonDtos.LessonSummary> listByDomain(Long domainId, Long userId) {
        return lessonRepository.findByDomainIdOrderBySequenceOrderAsc(domainId).stream()
                .map(lesson -> toSummary(lesson, userId))
                .toList();
    }

    private LessonDtos.LessonSummary toSummary(Lesson lesson, Long userId) {
        String status = null;
        if (userId != null) {
            status = progressRepository.findByUserIdAndLessonId(userId, lesson.getId())
                    .map(p -> p.getStatus().name())
                    .orElse(UserLessonProgress.Status.NOT_STARTED.name());
        }
        return new LessonDtos.LessonSummary(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getSummary(),
                lesson.getDifficulty(),
                lesson.getSequenceOrder(),
                lesson.getBaseXpReward(),
                lesson.getDomain().getName(),
                status
        );
    }

    /** Full lesson content plus its questions, with correct answers stripped for the client. */
    public LessonDtos.LessonDetail getDetail(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        List<QuestionDtos.QuestionPublic> questions = questionRepository.findByLessonId(lessonId).stream()
                .map(this::toPublicQuestion)
                .toList();

        return new LessonDtos.LessonDetail(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getSummary(),
                lesson.getContent(),
                lesson.getDifficulty(),
                lesson.getBaseXpReward(),
                lesson.getDomain().getName(),
                questions
        );
    }

    private QuestionDtos.QuestionPublic toPublicQuestion(Question q) {
        List<QuestionDtos.OptionPublic> options = q.getOptions().stream()
                .map(o -> new QuestionDtos.OptionPublic(o.getId(), o.getText()))
                .toList();
        return new QuestionDtos.QuestionPublic(q.getId(), q.getPrompt(), q.getType(), options);
    }

    public Optional<Lesson> find(Long id) {
        return lessonRepository.findById(id);
    }
}
