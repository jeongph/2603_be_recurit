package com.artinus.plugin.stub;

import com.artinus.subscription.service.port.GateResult;
import com.artinus.subscription.service.port.RandomGate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
@Profile("stub")
public class StubRandomGate implements RandomGate {

    @Override
    public GateResult request() {
        simulateLatency();
        return ThreadLocalRandom.current().nextBoolean()
                ? GateResult.ALLOWED
                : GateResult.DENIED;
    }

    private void simulateLatency() {
        try {
            long delay = 50 + ThreadLocalRandom.current().nextLong(150);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
