package com.artinus.history.controller;

import com.artinus.history.domain.EventOutcome;
import com.artinus.history.service.HistoryEntry;
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
    public static HistoryItemResponse from(HistoryEntry entry) {
        return new HistoryItemResponse(
                entry.occurredAt(),
                entry.channel().displayName(),
                entry.operation(),
                entry.from(),
                entry.to(),
                entry.outcome()
        );
    }
}
