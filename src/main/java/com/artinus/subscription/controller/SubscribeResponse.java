package com.artinus.subscription.controller;

import com.artinus.subscription.domain.SubscriptionState;
import com.artinus.subscription.service.ChangeOutcome;

public record SubscribeResponse(
        ChangeOutcome outcome,
        SubscriptionState currentState
) {
}
