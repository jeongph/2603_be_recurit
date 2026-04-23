package com.artinus.history.service;

import com.artinus.history.domain.SubscriptionEvent;
import com.artinus.history.repository.SubscriptionEventRepository;
import com.artinus.history.service.port.HistorySummarizer;
import com.artinus.history.service.port.SummarizerUnavailableException;
import com.artinus.subscription.domain.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistoryService {

    private static final Logger log = LoggerFactory.getLogger(HistoryService.class);

    private final SubscriptionEventRepository eventRepository;
    private final HistorySummarizer summarizer;
    private final int defaultLimit;

    public HistoryService(
            SubscriptionEventRepository eventRepository,
            HistorySummarizer summarizer,
            @Value("${history.summary.event-limit:20}") int defaultLimit) {
        this.eventRepository = eventRepository;
        this.summarizer = summarizer;
        this.defaultLimit = defaultLimit;
    }

    @Transactional(readOnly = true)
    public HistoryView query(HistoryQuery query) {
        PhoneNumber phoneNumber = PhoneNumber.of(query.phoneNumber());
        int limit = query.limit() > 0 ? query.limit() : defaultLimit;

        List<SubscriptionEvent> events = eventRepository
                .findByPhoneNumberOrderByOccurredAtDesc(phoneNumber)
                .stream()
                .limit(limit)
                .toList();

        SummaryOutcome outcome = buildSummary(events);

        return new HistoryView(phoneNumber.value(), events, outcome.summary, outcome.status);
    }

    private SummaryOutcome buildSummary(List<SubscriptionEvent> events) {
        try {
            String summary = summarizer.summarize(events);
            return new SummaryOutcome(summary, SummaryStatus.GENERATED);
        } catch (SummarizerUnavailableException e) {
            log.warn("summarizer unavailable, returning partial response", e);
            return new SummaryOutcome(null, SummaryStatus.UNAVAILABLE);
        } catch (RuntimeException e) {
            log.warn("summarizer threw unexpected exception, returning partial response", e);
            return new SummaryOutcome(null, SummaryStatus.UNAVAILABLE);
        }
    }

    private record SummaryOutcome(String summary, SummaryStatus status) {}
}
