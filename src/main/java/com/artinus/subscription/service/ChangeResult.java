package com.artinus.subscription.service;

import com.artinus.subscription.domain.SubscriptionState;

public record ChangeResult(
        ChangeOutcome outcome,
        SubscriptionState currentState
) {
}
