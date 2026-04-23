package com.artinus.plugin.csrng;

import com.artinus.subscription.service.port.GateResult;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("real-csrng")
@Tag("smoke")
class CsrngRandomGateSmokeTest {

    @Autowired CsrngRandomGate gate;

    @Test
    void actualCall_shouldReturnAllowedOrDenied() {
        GateResult result = gate.request();
        assertThat(result).isIn(GateResult.ALLOWED, GateResult.DENIED);
    }
}
