package com.artinus.support;

import com.artinus.history.domain.SubscriptionEvent;
import com.artinus.history.service.port.HistorySummarizer;
import com.artinus.history.service.port.SummarizerUnavailableException;

import java.util.List;

public class ExplodingSummarizer implements HistorySummarizer {

    @Override
    public String summarize(List<SubscriptionEvent> events) {
        throw new SummarizerUnavailableException("summarizer unavailable (test)", null);
    }
}
