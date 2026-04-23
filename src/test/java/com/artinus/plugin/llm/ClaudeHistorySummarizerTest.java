package com.artinus.plugin.llm;

import com.artinus.history.domain.EventOutcome;
import com.artinus.history.service.HistoryEntry;
import com.artinus.subscription.domain.Channel;
import com.artinus.subscription.domain.Operation;
import com.artinus.subscription.domain.PhoneNumber;
import com.artinus.subscription.domain.SubscriptionState;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClaudeHistorySummarizerTest {

    private final ClaudeApiClient client = mock(ClaudeApiClient.class);
    private final ClaudeHistorySummarizer summarizer = new ClaudeHistorySummarizer(client);

    @Test
    void summarize_shouldDelegateToClient_withPromptContainingEachEntry() {
        HistoryEntry entry = new HistoryEntry(
                PhoneNumber.of("010-1234-5678"),
                SubscriptionState.NONE, SubscriptionState.GENERAL,
                Channel.HOMEPAGE, Operation.SUBSCRIBE,
                EventOutcome.SUCCEEDED,
                Instant.parse("2026-04-01T00:00:00Z"));
        when(client.generateMessage(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("요약 결과");

        String result = summarizer.summarize(List.of(entry));

        assertThat(result).isEqualTo("요약 결과");

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).generateMessage(promptCaptor.capture());
        assertThat(promptCaptor.getValue())
                .contains("홈페이지")
                .contains("NONE -> GENERAL")
                .contains("SUBSCRIBE");
    }
}
