package com.artinus.subscription.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "subscription")
public class Subscription {

    @EmbeddedId
    private PhoneNumberId phoneNumberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SubscriptionState state;

    @Version
    @Column(nullable = false)
    private Long version;

    protected Subscription() {
    }

    private Subscription(PhoneNumber phoneNumber, SubscriptionState state) {
        this.phoneNumberId = new PhoneNumberId(phoneNumber.value());
        this.state = state;
    }

    public static Subscription of(PhoneNumber phoneNumber, SubscriptionState state) {
        return new Subscription(phoneNumber, state);
    }

    public SubscriptionChanged changeTo(SubscriptionState target, Channel channel, Operation op) {
        if (!channel.supports(op)) {
            throw new ChannelNotAllowedException(channel, op);
        }
        if (!this.state.canTransitionTo(target, op)) {
            throw new IllegalStateTransitionException(this.state, target, op);
        }
        SubscriptionState previous = this.state;
        this.state = target;
        return new SubscriptionChanged(
                this.phoneNumber(), previous, target, channel, op, Instant.now());
    }

    public PhoneNumber phoneNumber() {
        return PhoneNumber.of(phoneNumberId.value());
    }

    public SubscriptionState state() {
        return state;
    }

    public Long version() {
        return version;
    }
}
