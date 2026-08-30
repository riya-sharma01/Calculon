package com.calculon.controller;

import com.calculon.dto.AuthDtos;
import com.calculon.entity.User;
import com.calculon.security.JwtService;
import com.calculon.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        User user = userService.register(req.username(), req.email(), req.password(), req.displayName());
        return ResponseEntity.ok(toAuthResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        User user = userService.authenticate(req.usernameOrEmail(), req.password());
        return ResponseEntity.ok(toAuthResponse(user));
    }

    private AuthDtos.AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getUsername());
        var summary = new AuthDtos.UserSummary(
                user.getId(), user.getUsername(), user.getDisplayName(),
                user.getTotalXp(), user.getLevel(), user.getCurrentStreak());
        return new AuthDtos.AuthResponse(token, summary);
    }
}
