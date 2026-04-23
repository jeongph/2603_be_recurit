package com.artinus.history.controller;

import com.artinus.history.domain.SubscriptionEvent;
import com.artinus.history.repository.SubscriptionEventRepository;
import com.artinus.subscription.domain.Channel;
import com.artinus.subscription.domain.Operation;
import com.artinus.subscription.domain.PhoneNumber;
import com.artinus.subscription.domain.SubscriptionChanged;
import com.artinus.subscription.domain.SubscriptionState;
import com.artinus.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class HistoryControllerIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired SubscriptionEventRepository eventRepository;

    @BeforeEach void reset() {
        eventRepository.deleteAll();
    }

    @Test
    void getHistory_shouldReturnHistoryWithSummary() throws Exception {
        eventRepository.save(SubscriptionEvent.succeeded(new SubscriptionChanged(
                PhoneNumber.of("010-1111-2222"),
                SubscriptionState.NONE, SubscriptionState.GENERAL,
                Channel.HOMEPAGE, Operation.SUBSCRIBE, Instant.now())));

        mockMvc.perform(get("/api/v1/subscriptions/history")
                        .param("phoneNumber", "010-1111-2222"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("010-1111-2222"))
                .andExpect(jsonPath("$.history").isArray())
                .andExpect(jsonPath("$.history.length()").value(1))
                .andExpect(jsonPath("$.summaryStatus").value("GENERATED"));
    }
}
