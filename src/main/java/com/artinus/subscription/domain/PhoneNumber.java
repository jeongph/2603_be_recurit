package com.artinus.subscription.domain;

import java.util.regex.Pattern;

public record PhoneNumber(String value) {

    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("^01[016-9]-?\\d{3,4}-?\\d{4}$");

    public PhoneNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("전화번호가 비어있습니다.");
        }
        String normalized = normalize(value);
        if (!MOBILE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("국내 휴대폰 번호 형식이 아닙니다: " + value);
        }
        value = toCanonical(normalized);
    }

    public static PhoneNumber of(String raw) {
        return new PhoneNumber(raw);
    }

    private static String normalize(String raw) {
        return raw.replaceAll("\\s+", "");
    }

    private static String toCanonical(String normalized) {
        String digits = normalized.replace("-", "");
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        }
        return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
    }
}
