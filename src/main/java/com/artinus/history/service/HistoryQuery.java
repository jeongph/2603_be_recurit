package com.artinus.history.service;

public record HistoryQuery(
        String phoneNumber,
        int limit
) {
    public HistoryQuery {
        if (limit <= 0) limit = 20;
    }
}
