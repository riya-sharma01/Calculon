package com.calculon.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Sends email through Brevo's HTTPS API. An HTTP API is used instead of SMTP because Render's free tier
 * blocks outbound SMTP ports. If no API key is configured, nothing is sent: in development the reset link is
 * written to the log instead, so the feature can be tried locally without an email account.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private final String apiKey;
    private final String senderEmail;
    private final String senderName;
    private final boolean logLinks;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    public EmailService(@Value("${app.mail.brevo-api-key:}") String apiKey,
                        @Value("${app.mail.sender-email:}") String senderEmail,
                        @Value("${app.mail.sender-name:Calculon}") String senderName,
                        @Value("${app.mail.log-links:false}") boolean logLinks) {
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.logLinks = logLinks;
    }

    /** Sends the reset link. Runs in the background so the HTTP response is not delayed, and never throws. */
    public void sendPasswordReset(String toEmail, String displayName, String link) {
        if (apiKey.isBlank() || senderEmail.isBlank()) {
            if (logLinks) {
                log.info("Email is not configured. Password reset link (development only): {}", link);
            } else {
                log.warn("Email is not configured (set BREVO_API_KEY and MAIL_SENDER_EMAIL): reset email not sent.");
            }
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                send(toEmail, displayName, link);
            } catch (Exception e) {
                log.error("Failed to send the password reset email: {}", e.getMessage());
            }
        });
    }

    private void send(String toEmail, String displayName, String link) throws Exception {
        String name = displayName == null || displayName.isBlank() ? "there" : displayName;
        String text = "Hi " + name + ",\n\nUse this link to choose a new Calculon password. It works once and expires in 30 minutes:\n"
                + link + "\n\nIf you did not ask for this, you can ignore this email and your password stays the same.";
        String html = "<p>Hi " + HtmlUtils.htmlEscape(name) + ",</p>"
                + "<p>Use the button below to choose a new Calculon password. The link works once and expires in 30 minutes.</p>"
                + "<p><a href=\"" + link + "\" style=\"background:#FFC93C;color:#111;padding:10px 18px;border-radius:8px;"
                + "text-decoration:none;font-weight:bold\">Reset my password</a></p>"
                + "<p>If the button does not work, copy this address into your browser:<br>" + link + "</p>"
                + "<p>If you did not ask for this, you can ignore this email and your password stays the same.</p>";

        Map<String, Object> body = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", List.of(Map.of("email", toEmail, "name", name)),
                "subject", "Reset your Calculon password",
                "htmlContent", html,
                "textContent", text);

        HttpRequest request = HttpRequest.newBuilder(URI.create(BREVO_URL))
                .timeout(Duration.ofSeconds(15))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            log.error("Brevo rejected the email: HTTP {} {}", response.statusCode(), response.body());
        } else {
            log.info("Password reset email handed to Brevo.");
        }
    }
}
