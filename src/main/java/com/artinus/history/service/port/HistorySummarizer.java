package com.artinus.history.service.port;

import com.artinus.history.domain.SubscriptionEvent;

import java.util.List;

public interface HistorySummarizer {
    String summarize(List<SubscriptionEvent> events);
}
