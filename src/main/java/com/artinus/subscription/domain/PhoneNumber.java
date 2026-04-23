package com.artinus.subscription.domain;

import com.artinus.common.logging.PhoneNumberMasker;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.regex.Pattern;

@Embeddable
public record PhoneNumber(@Column(name = "phone_number", length = 16, nullable = false) String value)
        implements Serializable {

    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("^(010\\d{8}|01[16-9]\\d{7})$");

    public PhoneNumber {
        if (value == null || value.isBlank()) {
            throw new InvalidPhoneNumberException("전화번호가 비어있습니다.");
        }
        String digits = value.replaceAll("[\\s-]", "");
        if (!MOBILE_PATTERN.matcher(digits).matches()) {
            throw new InvalidPhoneNumberException(
                    "국내 휴대폰 번호 형식이 아닙니다: " + PhoneNumberMasker.mask(value));
        }
        value = toCanonical(digits);
    }

    public static PhoneNumber of(String raw) {
        return new PhoneNumber(raw);
    }

    private static String toCanonical(String digits) {
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        }
        return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
    }
}
