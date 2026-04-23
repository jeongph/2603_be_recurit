package com.artinus.subscription.domain;

public enum SubscriptionState {
    NONE,
    GENERAL,
    PREMIUM;

    public boolean canTransitionTo(SubscriptionState target, Operation op) {
        if (target == this) {
            return false;
        }
        return switch (op) {
            case SUBSCRIBE -> switch (this) {
                case NONE -> target == GENERAL || target == PREMIUM;
                case GENERAL -> target == PREMIUM;
                case PREMIUM -> false;
            };
            case UNSUBSCRIBE -> switch (this) {
                case PREMIUM -> target == GENERAL || target == NONE;
                case GENERAL -> target == NONE;
                case NONE -> false;
            };
        };
    }
}
