package com.project.public_safety_app.service;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SafetyReportServiceImpl {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    private static void validateUserName(String userName) {
        if (userName == null || userName.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        // Canonicalize the input to prevent encoding bypasses
        String normalizedUserName = Normalizer.normalize(userName, Normalizer.Form.NFKC);

        // Validate against the enhanced regex pattern
        if (!USERNAME_PATTERN.matcher(normalizedUserName).matches()) {
            throw new IllegalArgumentException("Username must be alphanumeric, between 3 to 30 characters, and not contain invalid characters");
        }
    }

    private static void validatePassword(String password, String userName) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must be at least 8 characters long, contain an uppercase letter, a lowercase letter, a number, and a special character");
        }
        if (password.toLowerCase().contains(userName.toLowerCase())) {
            throw new IllegalArgumentException("Password cannot contain the username");
        }
    }
}