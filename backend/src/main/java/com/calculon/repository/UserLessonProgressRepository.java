package com.calculon.repository;

import com.calculon.entity.UserLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, Long> {
    List<UserLessonProgress> findByUserId(Long userId);
    Optional<UserLessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);
    long countByUserIdAndStatus(Long userId, UserLessonProgress.Status status);
}
