package com.artinus.history.service.port;

import com.artinus.history.service.HistoryEntry;

import java.util.List;

public interface HistorySummarizer {
    /**
     * @param entries 요약 대상으로 사전 필터링된 이력(성공 이벤트, 시간 오름차순).
     *                호출자가 비어있지 않고 정렬된 목록을 보장한다.
     */
    String summarize(List<HistoryEntry> entries);
}
