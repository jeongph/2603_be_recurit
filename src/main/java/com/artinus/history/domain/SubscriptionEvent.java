package com.artinus.history.domain;

import com.artinus.subscription.domain.Channel;
import com.artinus.subscription.domain.Operation;
import com.artinus.subscription.domain.PhoneNumber;
import com.artinus.subscription.domain.SubscriptionChanged;
import com.artinus.subscription.domain.SubscriptionState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "subscription_event")
public class SubscriptionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, length = 16)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "state_from", nullable = false, length = 16)
    private SubscriptionState from;

    @Enumerated(EnumType.STRING)
    @Column(name = "state_to", nullable = false, length = 16)
    private SubscriptionState to;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false, length = 16)
    private Operation operation;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 16)
    private EventOutcome outcome;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected SubscriptionEvent() {
    }

    private SubscriptionEvent(
            String phoneNumber,
            SubscriptionState from,
            SubscriptionState to,
            Long channelId,
            Operation operation,
            EventOutcome outcome,
            Instant occurredAt) {
        this.phoneNumber = phoneNumber;
        this.from = from;
        this.to = to;
        this.channelId = channelId;
        this.operation = operation;
        this.outcome = outcome;
        this.occurredAt = occurredAt;
    }

    public static SubscriptionEvent succeeded(SubscriptionChanged event) {
        return new SubscriptionEvent(
                event.phoneNumber().value(),
                event.from(),
                event.to(),
                event.channel().id(),
                event.operation(),
                EventOutcome.SUCCEEDED,
                event.occurredAt());
    }

    public static SubscriptionEvent deniedByGate(
            PhoneNumber phoneNumber,
            SubscriptionState current,
            SubscriptionState attempted,
            Channel channel,
            Operation operation) {
        return new SubscriptionEvent(
                phoneNumber.value(),
                current,
                attempted,
                channel.id(),
                operation,
                EventOutcome.DENIED_BY_GATE,
                Instant.now());
    }

    public Long id() { return id; }
    public String phoneNumber() { return phoneNumber; }
    public SubscriptionState from() { return from; }
    public SubscriptionState to() { return to; }
    public Long channelId() { return channelId; }
    public Operation operation() { return operation; }
    public EventOutcome outcome() { return outcome; }
    public Instant occurredAt() { return occurredAt; }
}
