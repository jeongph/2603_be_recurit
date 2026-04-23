package com.artinus.subscription.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionStateTest {

    @ParameterizedTest(name = "[{index}] {0} -> {1} via {2} : {3}")
    @CsvSource({
            "NONE,    GENERAL, SUBSCRIBE,   true",
            "NONE,    PREMIUM, SUBSCRIBE,   true",
            "NONE,    NONE,    SUBSCRIBE,   false",
            "GENERAL, PREMIUM, SUBSCRIBE,   true",
            "GENERAL, GENERAL, SUBSCRIBE,   false",
            "GENERAL, NONE,    SUBSCRIBE,   false",
            "PREMIUM, PREMIUM, SUBSCRIBE,   false",
            "PREMIUM, GENERAL, SUBSCRIBE,   false",
            "PREMIUM, NONE,    SUBSCRIBE,   false",

            "PREMIUM, GENERAL, UNSUBSCRIBE, true",
            "PREMIUM, NONE,    UNSUBSCRIBE, true",
            "PREMIUM, PREMIUM, UNSUBSCRIBE, false",
            "GENERAL, NONE,    UNSUBSCRIBE, true",
            "GENERAL, GENERAL, UNSUBSCRIBE, false",
            "GENERAL, PREMIUM, UNSUBSCRIBE, false",
            "NONE,    NONE,    UNSUBSCRIBE, false",
            "NONE,    GENERAL, UNSUBSCRIBE, false",
            "NONE,    PREMIUM, UNSUBSCRIBE, false",
    })
    void canTransitionTo_returnsExpected(
            SubscriptionState from,
            SubscriptionState target,
            Operation op,
            boolean expected
    ) {
        assertThat(from.canTransitionTo(target, op)).isEqualTo(expected);
    }
}
