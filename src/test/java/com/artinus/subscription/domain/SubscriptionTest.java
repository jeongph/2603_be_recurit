package com.artinus.subscription.domain;

import org.junit.jupiter.api.Test;

import static com.artinus.subscription.domain.SubscriptionFixtures.aSubscription;
import static com.artinus.subscription.domain.SubscriptionState.GENERAL;
import static com.artinus.subscription.domain.SubscriptionState.NONE;
import static com.artinus.subscription.domain.SubscriptionState.PREMIUM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionTest {

    @Test
    void changeTo_shouldUpdateStateAndEmitEvent_whenTransitionValid() {
        Subscription s = aSubscription().state(NONE).build();

        SubscriptionChanged event = s.changeTo(GENERAL, Channel.HOMEPAGE, Operation.SUBSCRIBE);

        assertThat(s.state()).isEqualTo(GENERAL);
        assertThat(event.from()).isEqualTo(NONE);
        assertThat(event.to()).isEqualTo(GENERAL);
        assertThat(event.channel()).isEqualTo(Channel.HOMEPAGE);
        assertThat(event.operation()).isEqualTo(Operation.SUBSCRIBE);
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    void changeTo_shouldReject_whenTransitionInvalid() {
        Subscription s = aSubscription().state(PREMIUM).build();

        assertThatThrownBy(() -> s.changeTo(PREMIUM, Channel.HOMEPAGE, Operation.SUBSCRIBE))
                .isInstanceOf(IllegalStateTransitionException.class);
    }

    @Test
    void changeTo_shouldReject_whenChannelDoesNotSupportOperation() {
        Subscription s = aSubscription().state(NONE).build();

        assertThatThrownBy(() -> s.changeTo(GENERAL, Channel.CALL_CENTER, Operation.SUBSCRIBE))
                .isInstanceOf(ChannelNotAllowedException.class);
    }

    @Test
    void changeTo_shouldReject_whenUnsubscribeViaSubscribeOnlyChannel() {
        Subscription s = aSubscription().state(PREMIUM).build();

        assertThatThrownBy(() -> s.changeTo(GENERAL, Channel.NAVER, Operation.UNSUBSCRIBE))
                .isInstanceOf(ChannelNotAllowedException.class);
    }

    @Test
    void unsubscribe_shouldTransitionPremiumToGeneral() {
        Subscription s = aSubscription().state(PREMIUM).build();

        SubscriptionChanged event = s.changeTo(GENERAL, Channel.CALL_CENTER, Operation.UNSUBSCRIBE);

        assertThat(s.state()).isEqualTo(GENERAL);
        assertThat(event.from()).isEqualTo(PREMIUM);
        assertThat(event.to()).isEqualTo(GENERAL);
    }

    @Test
    void changeTo_shouldKeepStateUnchanged_whenTransitionInvalid() {
        Subscription s = aSubscription().state(PREMIUM).build();

        assertThatThrownBy(() -> s.changeTo(PREMIUM, Channel.HOMEPAGE, Operation.SUBSCRIBE))
                .isInstanceOf(IllegalStateTransitionException.class);

        assertThat(s.state()).isEqualTo(PREMIUM);
    }

    @Test
    void changeTo_shouldKeepStateUnchanged_whenChannelNotAllowed() {
        Subscription s = aSubscription().state(NONE).build();

        assertThatThrownBy(() -> s.changeTo(GENERAL, Channel.CALL_CENTER, Operation.SUBSCRIBE))
                .isInstanceOf(ChannelNotAllowedException.class);

        assertThat(s.state()).isEqualTo(NONE);
    }
}
