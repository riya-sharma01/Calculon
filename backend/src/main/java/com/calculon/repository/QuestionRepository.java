package com.calculon.repository;

import com.calculon.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByLessonId(Long lessonId);

    boolean existsByExternalId(String externalId);

    /** All loaded question ids in one query, so the seeder does not make one round trip per question. */
    @Query("select q.externalId from Question q where q.externalId is not null")
    Set<String> findAllExternalIds();
}
