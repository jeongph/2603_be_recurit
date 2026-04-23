package com.artinus.subscription.service;

import com.artinus.subscription.domain.SubscriptionState;

public record SubscribeCommand(
        String phoneNumber,
        Long channelId,
        SubscriptionState targetState
) {
}
