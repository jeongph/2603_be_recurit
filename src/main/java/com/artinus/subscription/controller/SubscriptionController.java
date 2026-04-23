package com.artinus.subscription.controller;

import com.artinus.subscription.service.ChangeResult;
import com.artinus.subscription.service.SubscribeCommand;
import com.artinus.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscription", description = "구독/해지 API")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "구독 신청",
               description = "회원의 구독 상태를 변경한다. 외부 난수 판정 결과에 따라 SUCCEEDED 또는 DENIED_BY_GATE 반환.")
    public SubscribeResponse subscribe(@Valid @RequestBody SubscribeRequest req) {
        ChangeResult result = service.subscribe(
                new SubscribeCommand(req.phoneNumber(), req.channelId(), req.targetState()));
        return new SubscribeResponse(result.outcome(), result.currentState());
    }

    @PostMapping("/cancel")
    @Operation(summary = "구독 해지",
               description = "회원의 구독을 해지(다운그레이드)한다.")
    public SubscribeResponse unsubscribe(@Valid @RequestBody SubscribeRequest req) {
        ChangeResult result = service.unsubscribe(
                new SubscribeCommand(req.phoneNumber(), req.channelId(), req.targetState()));
        return new SubscribeResponse(result.outcome(), result.currentState());
    }
}
