package com.artinus.plugin.llm;

import com.artinus.history.service.HistoryEntry;
import com.artinus.history.service.port.HistorySummarizer;
import com.artinus.history.service.port.SummarizerUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@Profile("real-llm")
public class ClaudeHistorySummarizer implements HistorySummarizer {

    private static final DateTimeFormatter KR =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ClaudeApiClient client;

    public ClaudeHistorySummarizer(ClaudeApiClient client) {
        this.client = client;
    }

    @Override
    @Retry(name = "llm", fallbackMethod = "fallback")
    @CircuitBreaker(name = "llm", fallbackMethod = "fallback")
    public String summarize(List<HistoryEntry> entries) {
        return client.generateMessage(buildPrompt(entries));
    }

    @SuppressWarnings("unused")
    private String fallback(List<HistoryEntry> entries, Throwable t) {
        throw new SummarizerUnavailableException("llm summarizer unavailable: " + t.getMessage(), t);
    }

    private String buildPrompt(List<HistoryEntry> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("아래 구독 이벤트 목록을 바탕으로 회원의 구독 변화를 한국어로 간결하게 한 단락으로 요약해 주세요. ")
          .append("각 날짜, 채널, 상태 변화를 서사 흐름으로 이어주세요. 비즈니스 추측이나 추가 설명은 넣지 마세요.\n\n");
        for (HistoryEntry e : entries) {
            LocalDate date = e.occurredAt().atZone(KST).toLocalDate();
            sb.append("- ").append(KR.format(date))
              .append(" / 채널: ").append(e.channel().displayName())
              .append(" / ").append(e.from()).append(" -> ").append(e.to())
              .append(" (").append(e.operation()).append(")\n");
        }
        return sb.toString();
    }
}
