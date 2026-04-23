package com.artinus.subscription.controller;

import com.artinus.subscription.service.ChangeResult;
import com.artinus.subscription.service.SubscribeCommand;
import com.artinus.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @PostMapping
    public SubscribeResponse subscribe(@Valid @RequestBody SubscribeRequest req) {
        ChangeResult result = service.subscribe(
                new SubscribeCommand(req.phoneNumber(), req.channelId(), req.targetState()));
        return new SubscribeResponse(result.outcome(), result.currentState());
    }

    @PostMapping("/cancel")
    public SubscribeResponse unsubscribe(@Valid @RequestBody SubscribeRequest req) {
        ChangeResult result = service.unsubscribe(
                new SubscribeCommand(req.phoneNumber(), req.channelId(), req.targetState()));
        return new SubscribeResponse(result.outcome(), result.currentState());
    }
}
