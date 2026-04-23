package com.artinus.history.service;

/**
 * limit 이 null 이면 {@link HistoryService} 가 설정 프로퍼티
 * <code>history.summary.event-limit</code> 기본값을 사용한다.
 */
public record HistoryQuery(
        String phoneNumber,
        Integer limit
) {
}
