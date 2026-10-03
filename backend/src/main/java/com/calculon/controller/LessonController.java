package com.calculon.controller;

import com.calculon.dto.LessonDtos;
import com.calculon.service.LessonService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/by-domain/{domainId}")
    public List<LessonDtos.LessonSummary> byDomain(@PathVariable Long domainId, Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        return lessonService.listByDomain(domainId, userId);
    }

    @GetMapping("/{lessonId}")
    public LessonDtos.LessonDetail detail(@PathVariable Long lessonId) {
        return lessonService.getDetail(lessonId);
    }
}
