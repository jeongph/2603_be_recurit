package com.artinus.subscription.domain;

public class SubscriptionNotFoundException extends RuntimeException {
    public SubscriptionNotFoundException(PhoneNumber phoneNumber) {
        super("구독 정보를 찾을 수 없습니다: " + phoneNumber.value());
    }
}
