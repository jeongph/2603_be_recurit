package com.artinus.common.logging;

import com.artinus.subscription.domain.PhoneNumber;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberMaskerTest {

    @Test
    void mask_shouldMaskMiddleDigits_givenCanonicalInput() {
        assertThat(PhoneNumberMasker.mask("010-1234-5678")).isEqualTo("010-****-5678");
        assertThat(PhoneNumberMasker.mask("01012345678")).isEqualTo("010-****-5678");
    }

    @Test
    void mask_shouldHandleTenDigitNumbers() {
        assertThat(PhoneNumberMasker.mask("011-123-4567")).isEqualTo("011-****-4567");
        assertThat(PhoneNumberMasker.mask("0111234567")).isEqualTo("011-****-4567");
    }

    @Test
    void mask_shouldStripNonDigitCharacters_fromRawInput() {
        // 정규화 정책: 영숫자 외 모든 문자 제거 → 앞 3자리 + 뒤 4자리 보존
        assertThat(PhoneNumberMasker.mask("+82 10-1234-5678")).isEqualTo("821-****-5678");
    }

    @Test
    void mask_shouldReturnAsterisks_whenDigitsBelowSeven() {
        assertThat(PhoneNumberMasker.mask("010")).isEqualTo("***");
        assertThat(PhoneNumberMasker.mask("010123")).isEqualTo("***");
    }

    @Test
    void mask_shouldReturnNull_whenRawInputIsNull() {
        String nullInput = null;
        assertThat(PhoneNumberMasker.mask(nullInput)).isNull();
    }

    @Test
    void mask_shouldReturnMaskedCanonical_whenGivenPhoneNumberValueObject() {
        PhoneNumber phoneNumber = PhoneNumber.of("010-1234-5678");
        assertThat(PhoneNumberMasker.mask(phoneNumber)).isEqualTo("010-****-5678");
    }

    @Test
    void mask_shouldReturnNull_whenPhoneNumberValueObjectIsNull() {
        PhoneNumber nullInput = null;
        assertThat(PhoneNumberMasker.mask(nullInput)).isNull();
    }
}
