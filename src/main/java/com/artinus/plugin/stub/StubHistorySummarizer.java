package com.artinus.plugin.stub;

import com.artinus.history.service.HistoryEntry;
import com.artinus.history.service.port.HistorySummarizer;
import com.artinus.subscription.domain.SubscriptionState;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@Profile("stub")
public class StubHistorySummarizer implements HistorySummarizer {

    private static final DateTimeFormatter KR_FORMAT =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Override
    public String summarize(List<HistoryEntry> entries) {
        return entries.stream()
                .map(this::formatSegment)
                .collect(Collectors.joining(", ", "", "."));
    }

    private String formatSegment(HistoryEntry entry) {
        LocalDate date = entry.occurredAt().atZone(KST).toLocalDate();
        return "%s %s에서 %s 상태로 변경".formatted(
                KR_FORMAT.format(date),
                entry.channel().displayName(),
                koreanStateName(entry.to()));
    }

    private String koreanStateName(SubscriptionState state) {
        return switch (state) {
            case NONE -> "구독 해지";
            case GENERAL -> "일반 구독";
            case PREMIUM -> "프리미엄 구독";
        };
    }
}
