package com.artinus.history.service;

import java.util.List;

public record HistoryView(
        String phoneNumber,
        List<HistoryEntry> entries,
        String summary,
        SummaryStatus summaryStatus
) {
}
