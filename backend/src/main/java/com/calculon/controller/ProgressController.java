package com.calculon.controller;

import com.calculon.dto.ProgressDtos;
import com.calculon.service.ProgressService;
import com.calculon.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;
    private final UserService userService;

    public ProgressController(ProgressService progressService, UserService userService) {
        this.progressService = progressService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public ProgressDtos.DashboardResponse dashboard(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return progressService.dashboard(userService.getById(userId));
    }

    @PostMapping("/complete-lesson")
    public ProgressDtos.LevelUpEvent completeLesson(@RequestBody ProgressDtos.LessonCompletionRequest request,
                                                      Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return progressService.completeLesson(userService.getById(userId), request.lessonId(), request.scorePercent());
    }
}
