package com.artinus.history.service;

import com.artinus.history.repository.SubscriptionEventRepository;
import com.artinus.history.service.port.HistorySummarizer;
import com.artinus.history.service.port.SummarizerUnavailableException;
import com.artinus.subscription.domain.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
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
        int limit = query.limit() != null ? query.limit() : defaultLimit;

        // 조회 결과는 최신 이벤트가 앞에 오는 내림차순. API 응답도 이 순서를 유지한다.
        List<HistoryEntry> entries = eventRepository
                .findByPhoneNumberOrderByOccurredAtDesc(phoneNumber)
                .stream()
                .limit(limit)
                .map(HistoryEntry::from)
                .toList();

        SummaryOutcome outcome = buildSummary(entries);

        return new HistoryView(phoneNumber.value(), entries, outcome.summary, outcome.status);
    }

    private SummaryOutcome buildSummary(List<HistoryEntry> entries) {
        // 요약은 서사 흐름이므로 시간 오름차순으로 전달한다.
        List<HistoryEntry> chronological = new ArrayList<>(entries);
        Collections.reverse(chronological);

        try {
            String summary = summarizer.summarize(chronological);
            return new SummaryOutcome(summary, SummaryStatus.GENERATED);
        } catch (SummarizerUnavailableException e) {
            log.warn("summarizer unavailable, returning partial response", e);
            return new SummaryOutcome(null, SummaryStatus.UNAVAILABLE);
        }
    }

    private record SummaryOutcome(String summary, SummaryStatus status) {}
}
