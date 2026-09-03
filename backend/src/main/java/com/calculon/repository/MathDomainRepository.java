package com.calculon.repository;

import com.calculon.entity.MathDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MathDomainRepository extends JpaRepository<MathDomain, Long> {

    Optional<MathDomain> findBySlug(String slug);
}