package com.artinus.subscription.domain;

public class IllegalStateTransitionException extends RuntimeException {
    public IllegalStateTransitionException(SubscriptionState from, SubscriptionState to, Operation op) {
        super("전이 불가: " + from + " -> " + to + " via " + op);
    }
}
