package com.artinus.plugin.stub;

import com.artinus.history.domain.EventOutcome;
import com.artinus.history.domain.SubscriptionEvent;
import com.artinus.history.service.port.HistorySummarizer;
import com.artinus.subscription.domain.Channel;
import com.artinus.subscription.domain.SubscriptionState;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@Profile("stub")
public class StubHistorySummarizer implements HistorySummarizer {

    private static final DateTimeFormatter KR_FORMAT =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Override
    public String summarize(List<SubscriptionEvent> events) {
        List<SubscriptionEvent> succeeded = events.stream()
                .filter(e -> e.outcome() == EventOutcome.SUCCEEDED)
                .toList();

        if (succeeded.isEmpty()) {
            return "구독 이력이 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < succeeded.size(); i++) {
            SubscriptionEvent e = succeeded.get(i);
            LocalDate date = e.occurredAt().atZone(KST).toLocalDate();
            String channelName = Channel.fromId(e.channelId()).displayName();
            String stateKr = koreanStateName(e.to());

            if (i == 0) {
                sb.append(KR_FORMAT.format(date))
                  .append(" ").append(channelName)
                  .append("을(를) 통해 ").append(stateKr).append("으로 가입한 뒤,");
            } else if (i == succeeded.size() - 1) {
                sb.append(" ").append(KR_FORMAT.format(date))
                  .append(" ").append(channelName)
                  .append("에서 ").append(stateKr).append(" 상태로 변경하였습니다.");
            } else {
                sb.append(" ").append(KR_FORMAT.format(date))
                  .append(" ").append(channelName)
                  .append("에서 ").append(stateKr).append("으로 변경,");
            }
        }

        return sb.toString();
    }

    private String koreanStateName(SubscriptionState state) {
        return switch (state) {
            case NONE -> "구독 해지";
            case GENERAL -> "일반 구독";
            case PREMIUM -> "프리미엄 구독";
        };
    }
}
