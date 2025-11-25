package com.user.UserService.common;

import org.springframework.stereotype.Component;

@Component
public class InputSanitizer {

    public String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        return input
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;")
                .replace("&", "&amp;")
                .trim();
    }

    public String sanitizeAndLimit(String input, int maxLength) {
        String sanitized = sanitize(input);
        if (sanitized != null && sanitized.length() > maxLength) {
            return sanitized.substring(0, maxLength);
        }
        return sanitized;
    }
}

