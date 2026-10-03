package com.calculon.controller;

import com.calculon.dto.QuestionDtos;
import com.calculon.service.QuestionService;
import com.calculon.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;

    public QuestionController(QuestionService questionService, UserService userService) {
        this.questionService = questionService;
        this.userService = userService;
    }

    @PostMapping("/answer")
    public QuestionDtos.AnswerResult answer(@RequestBody QuestionDtos.AnswerRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return questionService.submitAnswer(userService.getById(userId), request);
    }
}
