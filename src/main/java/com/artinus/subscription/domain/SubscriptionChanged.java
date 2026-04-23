package com.artinus.subscription.domain;

import java.time.Instant;

public record SubscriptionChanged(
        PhoneNumber phoneNumber,
        SubscriptionState from,
        SubscriptionState to,
        Channel channel,
        Operation operation,
        Instant occurredAt
) {
}
