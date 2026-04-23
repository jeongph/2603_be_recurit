package com.artinus.history.controller;

import com.artinus.history.service.HistoryView;
import com.artinus.history.service.SummaryStatus;

import java.util.List;

public record HistoryResponse(
        String phoneNumber,
        List<HistoryItemResponse> history,
        String summary,
        SummaryStatus summaryStatus
) {
    public static HistoryResponse from(HistoryView view) {
        return new HistoryResponse(
                view.phoneNumber(),
                view.entries().stream().map(HistoryItemResponse::from).toList(),
                view.summary(),
                view.summaryStatus()
        );
    }
}
