package com.artinus.subscription.service;

import com.artinus.history.domain.EventOutcome;
import com.artinus.history.domain.SubscriptionEvent;
import com.artinus.history.repository.SubscriptionEventRepository;
import com.artinus.subscription.domain.ChannelNotAllowedException;
import com.artinus.subscription.domain.IllegalStateTransitionException;
import com.artinus.subscription.domain.PhoneNumber;
import com.artinus.subscription.domain.Subscription;
import com.artinus.subscription.domain.SubscriptionState;
import com.artinus.subscription.repository.SubscriptionRepository;
import com.artinus.subscription.service.port.GateUnavailableException;
import com.artinus.subscription.service.port.RandomGate;
import com.artinus.support.AbstractIntegrationTest;
import com.artinus.support.FixedRandomGate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionServiceTest {

    @Nested
    class SuccessFlow extends AbstractIntegrationTest {
        @TestConfiguration
        static class Config {
            @Bean @Primary RandomGate gate() { return FixedRandomGate.alwaysAllow(); }
        }

        @Autowired SubscriptionService service;
        @Autowired SubscriptionRepository subscriptionRepository;
        @Autowired SubscriptionEventRepository eventRepository;

        @BeforeEach void reset() {
            eventRepository.deleteAll();
            subscriptionRepository.deleteAll();
        }

        @Test
        void subscribe_shouldCreateSubscription_whenNewPhoneNumberAndGateAllowed() {
            ChangeResult result = service.subscribe(
                    new SubscribeCommand("010-1234-5678", 1L, SubscriptionState.GENERAL));

            assertThat(result.outcome()).isEqualTo(ChangeOutcome.SUCCEEDED);
            assertThat(result.currentState()).isEqualTo(SubscriptionState.GENERAL);

            Subscription saved = subscriptionRepository
                    .findByPhoneNumber(PhoneNumber.of("010-1234-5678")).orElseThrow();
            assertThat(saved.state()).isEqualTo(SubscriptionState.GENERAL);

            java.util.List<SubscriptionEvent> events = eventRepository
                    .findByPhoneNumberOrderByOccurredAtDesc("010-1234-5678");
            assertThat(events).hasSize(1);
            assertThat(events.get(0).outcome()).isEqualTo(EventOutcome.SUCCEEDED);
        }

        @Test
        void unsubscribe_shouldTransitionPremiumToGeneral() {
            subscriptionRepository.save(Subscription.of(
                    PhoneNumber.of("010-1234-5678"), SubscriptionState.PREMIUM));

            ChangeResult result = service.unsubscribe(
                    new SubscribeCommand("010-1234-5678", 5L, SubscriptionState.GENERAL));

            assertThat(result.outcome()).isEqualTo(ChangeOutcome.SUCCEEDED);
            assertThat(result.currentState()).isEqualTo(SubscriptionState.GENERAL);
        }
    }

    @Nested
    class DenialFlow extends AbstractIntegrationTest {
        @TestConfiguration
        static class Config {
            @Bean @Primary RandomGate gate() { return FixedRandomGate.alwaysDeny(); }
        }

        @Autowired SubscriptionService service;
        @Autowired SubscriptionRepository subscriptionRepository;
        @Autowired SubscriptionEventRepository eventRepository;

        @BeforeEach void reset() {
            eventRepository.deleteAll();
            subscriptionRepository.deleteAll();
        }

        @Test
        void subscribe_shouldRecordDeniedEvent_whenGateDenies() {
            ChangeResult result = service.subscribe(
                    new SubscribeCommand("010-1234-5678", 1L, SubscriptionState.GENERAL));

            assertThat(result.outcome()).isEqualTo(ChangeOutcome.DENIED_BY_GATE);
            assertThat(result.currentState()).isEqualTo(SubscriptionState.NONE);
            assertThat(subscriptionRepository.count()).isEqualTo(0);
            assertThat(eventRepository.findByPhoneNumberOrderByOccurredAtDesc("010-1234-5678"))
                    .hasSize(1)
                    .allMatch(e -> e.outcome() == EventOutcome.DENIED_BY_GATE);
        }
    }

    @Nested
    class GateFailure extends AbstractIntegrationTest {
        @TestConfiguration
        static class Config {
            @Bean @Primary RandomGate gate() { return FixedRandomGate.alwaysThrows(); }
        }

        @Autowired SubscriptionService service;

        @Test
        void subscribe_shouldPropagateGateUnavailableException() {
            assertThatThrownBy(() -> service.subscribe(
                    new SubscribeCommand("010-1234-5678", 1L, SubscriptionState.GENERAL)))
                    .isInstanceOf(GateUnavailableException.class);
        }
    }

    @Nested
    class ValidationFailure extends AbstractIntegrationTest {
        @TestConfiguration
        static class Config {
            @Bean @Primary RandomGate gate() { return FixedRandomGate.alwaysAllow(); }
        }

        @Autowired SubscriptionService service;

        @Test
        void subscribe_shouldReject_whenChannelDoesNotSupportSubscribe() {
            assertThatThrownBy(() -> service.subscribe(
                    new SubscribeCommand("010-1234-5678", 5L, SubscriptionState.GENERAL)))
                    .isInstanceOf(ChannelNotAllowedException.class);
        }

        @Test
        void subscribe_shouldReject_whenTransitionNotAllowed() {
            assertThatThrownBy(() -> service.subscribe(
                    new SubscribeCommand("010-1234-5678", 1L, SubscriptionState.NONE)))
                    .isInstanceOf(IllegalStateTransitionException.class);
        }
    }
}
