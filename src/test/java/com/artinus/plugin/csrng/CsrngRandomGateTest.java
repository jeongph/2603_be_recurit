package com.artinus.plugin.csrng;

import com.artinus.subscription.service.port.GateResult;
import com.artinus.subscription.service.port.GateUnavailableException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CsrngRandomGateTest {

    private final CsrngHttpClient client = mock(CsrngHttpClient.class);
    private final CsrngRandomGate gate = new CsrngRandomGate(client);

    @Test
    void request_shouldReturnAllowed_whenRandomIsOne() {
        when(client.fetch()).thenReturn(new CsrngResponse("success", 1));

        assertThat(gate.request()).isEqualTo(GateResult.ALLOWED);
    }

    @Test
    void request_shouldReturnDenied_whenRandomIsZero() {
        when(client.fetch()).thenReturn(new CsrngResponse("success", 0));

        assertThat(gate.request()).isEqualTo(GateResult.DENIED);
    }

    @Test
    void request_shouldThrowGateUnavailable_whenRandomIsNull() {
        when(client.fetch()).thenReturn(new CsrngResponse("success", null));

        assertThatThrownBy(gate::request)
                .isInstanceOf(GateUnavailableException.class)
                .hasMessageContaining("missing random");
    }
}
