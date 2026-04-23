package com.artinus.history.service.port;

import com.artinus.history.service.HistoryEntry;

import java.util.List;

public interface HistorySummarizer {
    /**
     * @param entries 시간 오름차순(오래된 것 먼저)으로 정렬된 이력 엔트리 목록
     */
    String summarize(List<HistoryEntry> entries);
}
