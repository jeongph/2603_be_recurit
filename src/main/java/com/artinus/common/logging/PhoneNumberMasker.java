package com.artinus.common.logging;

public final class PhoneNumberMasker {

    private PhoneNumberMasker() {}

    public static String mask(String phoneNumber) {
        if (phoneNumber == null) return null;
        String digits = phoneNumber.replace("-", "");
        if (digits.length() < 7) return "***";
        return digits.substring(0, 3) + "-****-" + digits.substring(digits.length() - 4);
    }
}
