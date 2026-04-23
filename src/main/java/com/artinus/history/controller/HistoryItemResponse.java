package com.artinus.history.controller;

import com.artinus.history.domain.EventOutcome;
import com.artinus.history.domain.SubscriptionEvent;
import com.artinus.subscription.domain.Channel;
import com.artinus.subscription.domain.Operation;
import com.artinus.subscription.domain.SubscriptionState;

import java.time.Instant;

public record HistoryItemResponse(
        Instant occurredAt,
        String channel,
        Operation operation,
        SubscriptionState from,
        SubscriptionState to,
        EventOutcome outcome
) {
    public static HistoryItemResponse from(SubscriptionEvent event) {
        return new HistoryItemResponse(
                event.occurredAt(),
                Channel.fromId(event.channelId()).displayName(),
                event.operation(),
                event.from(),
                event.to(),
                event.outcome()
        );
    }
}
