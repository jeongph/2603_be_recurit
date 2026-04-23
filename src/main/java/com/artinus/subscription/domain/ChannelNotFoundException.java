package com.artinus.subscription.domain;

public class ChannelNotFoundException extends RuntimeException {
    public ChannelNotFoundException(Long id) {
        super("Channel not found: id=" + id);
    }
}
