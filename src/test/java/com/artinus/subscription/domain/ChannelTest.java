package com.artinus.subscription.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChannelTest {

    @Test
    void fromId_returnsChannel_forKnownId() {
        assertThat(Channel.fromId(1L)).isEqualTo(Channel.HOMEPAGE);
        assertThat(Channel.fromId(3L)).isEqualTo(Channel.NAVER);
        assertThat(Channel.fromId(6L)).isEqualTo(Channel.EMAIL);
    }

    @Test
    void fromId_throws_forUnknownId() {
        assertThatThrownBy(() -> Channel.fromId(99L))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    void homepage_supportsBoth_subscribeAndUnsubscribe() {
        assertThat(Channel.HOMEPAGE.supports(Operation.SUBSCRIBE)).isTrue();
        assertThat(Channel.HOMEPAGE.supports(Operation.UNSUBSCRIBE)).isTrue();
    }

    @Test
    void naver_supportsSubscribeOnly() {
        assertThat(Channel.NAVER.supports(Operation.SUBSCRIBE)).isTrue();
        assertThat(Channel.NAVER.supports(Operation.UNSUBSCRIBE)).isFalse();
    }

    @Test
    void callCenter_supportsUnsubscribeOnly() {
        assertThat(Channel.CALL_CENTER.supports(Operation.SUBSCRIBE)).isFalse();
        assertThat(Channel.CALL_CENTER.supports(Operation.UNSUBSCRIBE)).isTrue();
    }
}
