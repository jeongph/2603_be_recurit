package com.artinus.subscription.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberTest {

    @ParameterizedTest
    @ValueSource(strings = {"010-1234-5678", "01012345678", "011-123-4567", "0111234567"})
    void should_accept_valid_korean_mobile_numbers(String raw) {
        PhoneNumber phoneNumber = PhoneNumber.of(raw);
        assertThat(phoneNumber.value()).isNotBlank();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "010", "02-123-4567", "abc", "+821012345678", "010-1234-567"})
    void should_reject_non_korean_mobile_format(String raw) {
        assertThatThrownBy(() -> PhoneNumber.of(raw))
                .isInstanceOf(InvalidPhoneNumberException.class);
    }

    @Test
    void should_normalize_hyphens_to_canonical_form() {
        PhoneNumber normalized = PhoneNumber.of("01012345678");
        assertThat(normalized.value()).isEqualTo("010-1234-5678");
    }

    @Test
    void should_reject_null() {
        assertThatThrownBy(() -> PhoneNumber.of(null))
                .isInstanceOf(InvalidPhoneNumberException.class);
    }

    @Test
    void should_have_value_equality() {
        assertThat(PhoneNumber.of("010-1234-5678"))
                .isEqualTo(PhoneNumber.of("01012345678"));
    }
}
