package com.artinus.subscription.domain;

public class ChannelNotAllowedException extends RuntimeException {
    public ChannelNotAllowedException(Channel channel, Operation op) {
        super("채널 " + channel.displayName() + " 은(는) " + op + " 를 지원하지 않습니다.");
    }
}
