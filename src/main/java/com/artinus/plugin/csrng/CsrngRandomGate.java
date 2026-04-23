package com.artinus.plugin.csrng;

import com.artinus.subscription.service.port.GateResult;
import com.artinus.subscription.service.port.GateUnavailableException;
import com.artinus.subscription.service.port.RandomGate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("real-csrng")
public class CsrngRandomGate implements RandomGate {

    private static final Logger log = LoggerFactory.getLogger(CsrngRandomGate.class);

    private final CsrngHttpClient client;

    public CsrngRandomGate(CsrngHttpClient client) {
        this.client = client;
    }

    @Override
    @Retry(name = "csrng", fallbackMethod = "fallback")
    @CircuitBreaker(name = "csrng", fallbackMethod = "fallback")
    public GateResult request() {
        CsrngResponse response = client.fetch();
        if (response.random() == null) {
            throw new GateUnavailableException("csrng response missing random value");
        }
        log.debug("csrng random={}", response.random());
        return response.random() == 1 ? GateResult.ALLOWED : GateResult.DENIED;
    }

    @SuppressWarnings("unused")
    private GateResult fallback(Throwable t) {
        throw new GateUnavailableException("csrng gate unavailable: " + t.getMessage(), t);
    }
}
