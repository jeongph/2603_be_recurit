package com.artinus.subscription.controller;

import com.artinus.subscription.service.port.RandomGate;
import com.artinus.support.AbstractIntegrationTest;
import com.artinus.support.FixedRandomGate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class SubscriptionControllerIT extends AbstractIntegrationTest {

    @TestConfiguration
    static class GateConfig {
        @Bean @Primary RandomGate gate() { return FixedRandomGate.alwaysAllow(); }
    }

    @Autowired MockMvc mockMvc;

    @Test
    void subscribe_shouldReturn200_andOutcome() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions")
                        .contentType("application/json")
                        .content("""
                                {
                                  "phoneNumber": "010-1111-2222",
                                  "channelId": 1,
                                  "targetState": "GENERAL"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outcome").value("SUCCEEDED"))
                .andExpect(jsonPath("$.currentState").value("GENERAL"));
    }

    @Test
    void subscribe_shouldReturn400_whenChannelNotAllowed() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions")
                        .contentType("application/json")
                        .content("""
                                {
                                  "phoneNumber": "010-1111-2222",
                                  "channelId": 5,
                                  "targetState": "GENERAL"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void subscribe_shouldReturn400_whenInvalidPhoneNumber() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions")
                        .contentType("application/json")
                        .content("""
                                {
                                  "phoneNumber": "02-123-4567",
                                  "channelId": 1,
                                  "targetState": "GENERAL"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
