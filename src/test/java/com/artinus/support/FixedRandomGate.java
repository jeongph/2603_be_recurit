package com.artinus.support;

import com.artinus.subscription.service.port.GateResult;
import com.artinus.subscription.service.port.GateUnavailableException;
import com.artinus.subscription.service.port.RandomGate;

public class FixedRandomGate implements RandomGate {

    private final GateResult fixedResult;
    private final RuntimeException throwOnRequest;

    private FixedRandomGate(GateResult fixedResult, RuntimeException throwOnRequest) {
        this.fixedResult = fixedResult;
        this.throwOnRequest = throwOnRequest;
    }

    public static FixedRandomGate alwaysAllow() {
        return new FixedRandomGate(GateResult.ALLOWED, null);
    }

    public static FixedRandomGate alwaysDeny() {
        return new FixedRandomGate(GateResult.DENIED, null);
    }

    public static FixedRandomGate alwaysThrows() {
        return new FixedRandomGate(null, new GateUnavailableException("gate unavailable (test)"));
    }

    @Override
    public GateResult request() {
        if (throwOnRequest != null) {
            throw throwOnRequest;
        }
        return fixedResult;
    }
}
