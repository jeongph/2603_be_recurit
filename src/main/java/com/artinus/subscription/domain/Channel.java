package com.artinus.subscription.domain;

import java.util.HashMap;
import java.util.Map;

public enum Channel {
    HOMEPAGE(1L, "홈페이지", ChannelCapability.BOTH),
    MOBILE_APP(2L, "모바일앱", ChannelCapability.BOTH),
    NAVER(3L, "네이버", ChannelCapability.SUBSCRIBE_ONLY),
    SKT(4L, "SKT", ChannelCapability.SUBSCRIBE_ONLY),
    CALL_CENTER(5L, "콜센터", ChannelCapability.UNSUBSCRIBE_ONLY),
    EMAIL(6L, "이메일", ChannelCapability.UNSUBSCRIBE_ONLY);

    private static final Map<Long, Channel> BY_ID;

    static {
        Map<Long, Channel> map = new HashMap<>();
        for (Channel c : values()) {
            if (map.put(c.id, c) != null) {
                throw new IllegalStateException("Duplicate channel id: " + c.id);
            }
        }
        BY_ID = Map.copyOf(map);
    }

    private final Long id;
    private final String displayName;
    private final ChannelCapability capability;

    Channel(Long id, String displayName, ChannelCapability capability) {
        this.id = id;
        this.displayName = displayName;
        this.capability = capability;
    }

    public Long id() { return id; }
    public String displayName() { return displayName; }

    public boolean supports(Operation op) {
        return capability.allows(op);
    }

    public static Channel fromId(Long id) {
        Channel channel = BY_ID.get(id);
        if (channel == null) {
            throw new ChannelNotFoundException(id);
        }
        return channel;
    }
}
