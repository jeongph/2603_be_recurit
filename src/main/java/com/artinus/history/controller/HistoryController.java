package com.artinus.history.controller;

import com.artinus.history.service.HistoryQuery;
import com.artinus.history.service.HistoryService;
import com.artinus.history.service.HistoryView;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions/history")
public class HistoryController {

    private final HistoryService service;

    public HistoryController(HistoryService service) {
        this.service = service;
    }

    @GetMapping
    public HistoryResponse query(
            @RequestParam @NotBlank String phoneNumber,
            @RequestParam(required = false) Integer limit) {
        HistoryView view = service.query(new HistoryQuery(phoneNumber, limit));
        return HistoryResponse.from(view);
    }
}
