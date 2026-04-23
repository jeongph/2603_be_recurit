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
    private PhoneNumber phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SubscriptionState state;

    @Version
    @Column(nullable = false)
    private Long version;

    protected Subscription() {
    }

    private Subscription(PhoneNumber phoneNumber, SubscriptionState state) {
        this.phoneNumber = phoneNumber;
        this.state = state;
    }

    public static Subscription of(PhoneNumber phoneNumber, SubscriptionState state) {
        return new Subscription(phoneNumber, state);
    }

    public SubscriptionChanged changeTo(SubscriptionState target, Channel channel, Operation op,
                                        Instant occurredAt) {
        // 채널 허용 검증이 먼저: "채널이 이 operation 자체를 못 하면 상태 전이는 볼 필요도 없다"
        if (!channel.supports(op)) {
            throw new ChannelNotAllowedException(channel, op);
        }
        if (!this.state.canTransitionTo(target, op)) {
            throw new IllegalStateTransitionException(this.state, target, op);
        }
        SubscriptionState previous = this.state;
        this.state = target;
        return new SubscriptionChanged(phoneNumber, previous, target, channel, op, occurredAt);
    }

    public PhoneNumber phoneNumber() {
        return phoneNumber;
    }

    public SubscriptionState state() {
        return state;
    }

    public Long version() {
        return version;
    }
}
