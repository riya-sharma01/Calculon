package com.calculon.repository;

import com.calculon.entity.UserQuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserQuestionAttemptRepository extends JpaRepository<UserQuestionAttempt, Long> {
    Optional<UserQuestionAttempt> findByUserIdAndQuestionId(Long userId, Long questionId);
}
