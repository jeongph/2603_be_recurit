package com.artinus.common.logging;

import com.artinus.subscription.domain.PhoneNumber;

public final class PhoneNumberMasker {

    private PhoneNumberMasker() {}

    public static String mask(PhoneNumber phoneNumber) {
        return phoneNumber == null ? null : mask(phoneNumber.value());
    }

    public static String mask(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() < 7) return "***";
        return digits.substring(0, 3) + "-****-" + digits.substring(digits.length() - 4);
    }
}
