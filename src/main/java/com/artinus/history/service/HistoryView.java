package com.artinus.history.service;

import com.artinus.history.domain.SubscriptionEvent;

import java.util.List;

public record HistoryView(
        String phoneNumber,
        List<SubscriptionEvent> events,
        String summary,
        SummaryStatus summaryStatus
) {
}
