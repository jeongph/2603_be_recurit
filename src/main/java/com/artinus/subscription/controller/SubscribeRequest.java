package com.artinus.subscription.controller;

import com.artinus.subscription.domain.SubscriptionState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubscribeRequest(
        @NotBlank String phoneNumber,
        @NotNull Long channelId,
        @NotNull SubscriptionState targetState
) {
}
