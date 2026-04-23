package com.artinus.history.service;

import com.artinus.history.domain.EventOutcome;
import com.artinus.history.domain.SubscriptionEvent;
import com.artinus.subscription.domain.Channel;
import com.artinus.subscription.domain.Operation;
import com.artinus.subscription.domain.PhoneNumber;
import com.artinus.subscription.domain.SubscriptionState;

import java.time.Instant;

public record HistoryEntry(
        PhoneNumber phoneNumber,
        SubscriptionState from,
        SubscriptionState to,
        Channel channel,
        Operation operation,
        EventOutcome outcome,
        Instant occurredAt
) {
    public static HistoryEntry from(SubscriptionEvent event) {
        return new HistoryEntry(
                event.phoneNumber(),
                event.from(),
                event.to(),
                Channel.fromId(event.channelId()),
                event.operation(),
                event.outcome(),
                event.occurredAt());
    }
}
