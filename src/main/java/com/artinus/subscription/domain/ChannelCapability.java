package com.artinus.subscription.domain;

import java.util.Set;

public enum ChannelCapability {
    BOTH(Set.of(Operation.SUBSCRIBE, Operation.UNSUBSCRIBE)),
    SUBSCRIBE_ONLY(Set.of(Operation.SUBSCRIBE)),
    UNSUBSCRIBE_ONLY(Set.of(Operation.UNSUBSCRIBE));

    private final Set<Operation> allowed;

    ChannelCapability(Set<Operation> allowed) {
        this.allowed = allowed;
    }

    public boolean allows(Operation op) {
        return allowed.contains(op);
    }
}
