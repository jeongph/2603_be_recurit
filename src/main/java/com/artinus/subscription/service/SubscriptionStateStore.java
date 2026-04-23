package com.artinus.subscription.service;

import com.artinus.history.domain.SubscriptionEvent;
import com.artinus.history.repository.SubscriptionEventRepository;
import com.artinus.subscription.domain.Channel;
import com.artinus.subscription.domain.Operation;
import com.artinus.subscription.domain.PhoneNumber;
import com.artinus.subscription.domain.Subscription;
import com.artinus.subscription.domain.SubscriptionChanged;
import com.artinus.subscription.domain.SubscriptionState;
import com.artinus.subscription.repository.SubscriptionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class SubscriptionStateStore {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionEventRepository eventRepository;

    public SubscriptionStateStore(SubscriptionRepository subscriptionRepository,
                                  SubscriptionEventRepository eventRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public SubscriptionState currentStateOf(PhoneNumber phoneNumber) {
        return subscriptionRepository.findByPhoneNumber(phoneNumber)
                .map(Subscription::state)
                .orElse(SubscriptionState.NONE);
    }

    @Transactional
    public ChangeResult commitChange(PhoneNumber phoneNumber, SubscriptionState target,
                                     Channel channel, Operation op, Instant occurredAt) {
        Subscription subscription = subscriptionRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> Subscription.of(phoneNumber, SubscriptionState.NONE));
        SubscriptionChanged event = subscription.changeTo(target, channel, op, occurredAt);
        subscriptionRepository.save(subscription);
        eventRepository.save(SubscriptionEvent.succeeded(event));
        return new ChangeResult(ChangeOutcome.SUCCEEDED, subscription.state());
    }

    @Transactional
    public ChangeResult recordDenial(PhoneNumber phoneNumber, SubscriptionState current,
                                     SubscriptionState attempted, Channel channel, Operation op,
                                     Instant occurredAt) {
        eventRepository.save(SubscriptionEvent.deniedByGate(
                phoneNumber, current, attempted, channel, op, occurredAt));
        return new ChangeResult(ChangeOutcome.DENIED_BY_GATE, current);
    }
}
