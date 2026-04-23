package com.artinus.history.service;

import com.artinus.history.domain.SubscriptionEvent;
import com.artinus.history.repository.SubscriptionEventRepository;
import com.artinus.history.service.port.HistorySummarizer;
import com.artinus.subscription.domain.Channel;
import com.artinus.subscription.domain.Operation;
import com.artinus.subscription.domain.PhoneNumber;
import com.artinus.subscription.domain.SubscriptionChanged;
import com.artinus.subscription.domain.SubscriptionState;
import com.artinus.support.AbstractIntegrationTest;
import com.artinus.support.ExplodingSummarizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class HistoryServiceTest {

    @Nested
    class SuccessFlow extends AbstractIntegrationTest {
        @Autowired HistoryService service;
        @Autowired SubscriptionEventRepository eventRepository;

        @BeforeEach void reset() {
            eventRepository.deleteAll();
        }

        @Test
        void query_shouldReturnEventsAndSummary_whenEventsExist() {
            eventRepository.save(SubscriptionEvent.succeeded(new SubscriptionChanged(
                    PhoneNumber.of("010-1111-2222"),
                    SubscriptionState.NONE, SubscriptionState.GENERAL,
                    Channel.HOMEPAGE, Operation.SUBSCRIBE, Instant.now())));

            HistoryView view = service.query(new HistoryQuery("010-1111-2222", null));

            assertThat(view.entries()).hasSize(1);
            assertThat(view.summary())
                    .contains("홈페이지")
                    .endsWith(".");
            assertThat(view.summaryStatus()).isEqualTo(SummaryStatus.GENERATED);
        }

        @Test
        void query_shouldReturnEmpty_whenNoEvents() {
            HistoryView view = service.query(new HistoryQuery("010-9999-9999", null));

            assertThat(view.entries()).isEmpty();
            assertThat(view.summary()).contains("이력이 없");
            assertThat(view.summaryStatus()).isEqualTo(SummaryStatus.GENERATED);
        }
    }

    @Nested
    class SummarizerFailure extends AbstractIntegrationTest {
        @TestConfiguration
        static class Config {
            @Bean @Primary HistorySummarizer summarizer() { return new ExplodingSummarizer(); }
        }

        @Autowired HistoryService service;
        @Autowired SubscriptionEventRepository eventRepository;

        @BeforeEach void reset() {
            eventRepository.deleteAll();
        }

        @Test
        void query_shouldReturnEventsWithUnavailableSummary_whenSummarizerFails() {
            eventRepository.save(SubscriptionEvent.succeeded(new SubscriptionChanged(
                    PhoneNumber.of("010-1111-2222"),
                    SubscriptionState.NONE, SubscriptionState.GENERAL,
                    Channel.HOMEPAGE, Operation.SUBSCRIBE, Instant.now())));

            HistoryView view = service.query(new HistoryQuery("010-1111-2222", null));

            assertThat(view.entries()).hasSize(1);
            assertThat(view.summary()).isNull();
            assertThat(view.summaryStatus()).isEqualTo(SummaryStatus.UNAVAILABLE);
        }
    }
}
