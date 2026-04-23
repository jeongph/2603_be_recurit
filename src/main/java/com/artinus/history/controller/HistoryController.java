package com.artinus.history.controller;

import com.artinus.history.service.HistoryQuery;
import com.artinus.history.service.HistoryService;
import com.artinus.history.service.HistoryView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions/history")
@Tag(name = "Subscription History", description = "구독 이력 조회 및 LLM 요약")
public class HistoryController {

    private final HistoryService service;

    public HistoryController(HistoryService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "구독 이력 조회",
               description = "회원의 구독 이력과 LLM 기반 자연어 요약을 반환한다. 요약 실패 시 summaryStatus=UNAVAILABLE.")
    public HistoryResponse query(
            @RequestParam @NotBlank String phoneNumber,
            @RequestParam(required = false) Integer limit) {
        HistoryView view = service.query(new HistoryQuery(phoneNumber, limit));
        return HistoryResponse.from(view);
    }
}
