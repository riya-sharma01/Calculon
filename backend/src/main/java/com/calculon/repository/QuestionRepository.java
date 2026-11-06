package com.calculon.repository;

import com.calculon.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByLessonId(Long lessonId);

    boolean existsByExternalId(String externalId);

    /** A lesson's questions with their options in one query, so the seeder can compare them with the JSON. */
    @Query("select distinct q from Question q left join fetch q.options where q.lesson.id = :lessonId")
    List<Question> findByLessonIdWithOptions(@Param("lessonId") Long lessonId);

    /** All loaded question ids in one query, so the seeder does not make one round trip per question. */
    @Query("select q.externalId from Question q where q.externalId is not null")
    Set<String> findAllExternalIds();
}
