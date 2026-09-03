package com.calculon.repository;

import com.calculon.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByDomainIdOrderBySequenceOrderAsc(Long domainId);

    Optional<Lesson> findByDomainIdAndTitle(Long domainId, String title);
}