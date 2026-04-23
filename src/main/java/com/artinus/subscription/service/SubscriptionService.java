package com.artinus.subscription.service;

import com.artinus.subscription.domain.Channel;
import com.artinus.subscription.domain.ChannelNotAllowedException;
import com.artinus.subscription.domain.IllegalStateTransitionException;
import com.artinus.subscription.domain.Operation;
import com.artinus.subscription.domain.PhoneNumber;
import com.artinus.subscription.domain.SubscriptionState;
import com.artinus.subscription.service.port.GateResult;
import com.artinus.subscription.service.port.RandomGate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class SubscriptionService {

    private final SubscriptionStateStore store;
    private final RandomGate randomGate;
    private final Clock clock;

    public SubscriptionService(SubscriptionStateStore store, RandomGate randomGate, Clock clock) {
        this.store = store;
        this.randomGate = randomGate;
        this.clock = clock;
    }

    public ChangeResult subscribe(SubscribeCommand cmd) {
        return change(cmd, Operation.SUBSCRIBE);
    }

    public ChangeResult unsubscribe(SubscribeCommand cmd) {
        return change(cmd, Operation.UNSUBSCRIBE);
    }

    private ChangeResult change(SubscribeCommand cmd, Operation op) {
        PhoneNumber phoneNumber = PhoneNumber.of(cmd.phoneNumber());
        Channel channel = Channel.fromId(cmd.channelId());
        SubscriptionState target = cmd.targetState();

        SubscriptionState current = store.currentStateOf(phoneNumber);
        validate(current, target, channel, op);

        // Gate 호출은 트랜잭션 바깥. 네트워크 I/O 구간에 DB 락을 점유하지 않는다.
        GateResult gate = randomGate.request();

        return gate == GateResult.ALLOWED
                ? store.commitChange(phoneNumber, target, channel, op)
                : store.recordDenial(phoneNumber, current, target, channel, op, Instant.now(clock));
    }

    private void validate(SubscriptionState current, SubscriptionState target,
                          Channel channel, Operation op) {
        if (!channel.supports(op)) throw new ChannelNotAllowedException(channel, op);
        if (!current.canTransitionTo(target, op))
            throw new IllegalStateTransitionException(current, target, op);
    }
}
