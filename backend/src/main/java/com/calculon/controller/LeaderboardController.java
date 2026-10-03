package com.calculon.controller;

import com.calculon.entity.User;
import com.calculon.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final UserRepository userRepository;

    public LeaderboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record LeaderboardEntry(String displayName, Long totalXp, Integer level, Integer currentStreak) {}

    @GetMapping
    public List<LeaderboardEntry> top20() {
        return userRepository.findTop20ByOrderByTotalXpDesc().stream()
                .map(u -> new LeaderboardEntry(u.getDisplayName(), u.getTotalXp(), u.getLevel(), u.getCurrentStreak()))
                .toList();
    }
}
