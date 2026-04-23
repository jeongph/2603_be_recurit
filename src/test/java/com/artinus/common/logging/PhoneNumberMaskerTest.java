package com.artinus.common.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberMaskerTest {

    @Test
    void mask_shouldMaskMiddleDigits() {
        assertThat(PhoneNumberMasker.mask("010-1234-5678")).isEqualTo("010-****-5678");
        assertThat(PhoneNumberMasker.mask("01012345678")).isEqualTo("010-****-5678");
    }

    @Test
    void mask_shouldReturnAsterisks_whenTooShort() {
        assertThat(PhoneNumberMasker.mask("010")).isEqualTo("***");
    }

    @Test
    void mask_shouldReturnNull_whenInputNull() {
        assertThat(PhoneNumberMasker.mask(null)).isNull();
    }
}
