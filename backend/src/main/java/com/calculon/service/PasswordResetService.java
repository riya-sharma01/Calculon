package com.calculon.service;

import com.calculon.entity.PasswordResetToken;
import com.calculon.entity.User;
import com.calculon.repository.PasswordResetTokenRepository;
import com.calculon.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * "Forgot password" flow.
 *  1. requestReset: if the email belongs to an account, create a random one-time token, store only its hash,
 *     and email a link containing the token. The caller always gets the same answer, so nobody can use this to
 *     find out which emails are registered.
 *  2. resetPassword: the token must exist, be unused and not expired. It sets the new password and uses up the token.
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    static final Duration TOKEN_LIFETIME = Duration.ofMinutes(30);
    /** At most one reset email per account per minute, so the form cannot be used to spam an inbox. */
    static final Duration COOLDOWN = Duration.ofSeconds(60);
    static final String INVALID_LINK = "This reset link is invalid or has expired. Please request a new one.";

    private final SecureRandom random = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final String frontendUrl;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                UserService userService,
                                EmailService emailService,
                                @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.userService = userService;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
    }

    @Transactional
    public void requestReset(String email) {
        Optional<User> found = userRepository.findByEmail(email.trim());
        if (found.isEmpty()) {
            log.info("Password reset requested for an email that has no account.");
            return; // same visible result as success
        }
        User user = found.get();

        Optional<PasswordResetToken> latest = tokenRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId());
        if (latest.isPresent() && latest.get().getCreatedAt().isAfter(LocalDateTime.now().minus(COOLDOWN))) {
            log.info("Password reset requested again within the cooldown; ignored.");
            return;
        }

        tokenRepository.deleteByUserId(user.getId()); // only the newest link works
        String rawToken = newToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(LocalDateTime.now().plus(TOKEN_LIFETIME));
        tokenRepository.save(token);

        emailService.sendPasswordReset(user.getEmail(), user.getDisplayName(),
                frontendUrl + "/reset-password?token=" + rawToken);
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByTokenHash(hash(rawToken.trim()))
                .orElseThrow(() -> new IllegalArgumentException(INVALID_LINK));
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(INVALID_LINK);
        }
        User user = token.getUser();
        userService.changePassword(user, newPassword);
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);
        tokenRepository.deleteByUserId(user.getId()); // clean up: this and any other links are now dead
    }

    /** 32 random bytes as URL-safe text (43 characters). */
    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e); // SHA-256 is always available in Java
        }
    }
}
