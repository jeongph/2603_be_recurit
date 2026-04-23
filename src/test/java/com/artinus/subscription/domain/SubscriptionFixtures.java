package com.artinus.subscription.domain;

public class SubscriptionFixtures {

    public static SubscriptionBuilder aSubscription() {
        return new SubscriptionBuilder();
    }

    public static class SubscriptionBuilder {
        private PhoneNumber phoneNumber = PhoneNumber.of("010-1234-5678");
        private SubscriptionState state = SubscriptionState.NONE;

        public SubscriptionBuilder phoneNumber(String raw) {
            this.phoneNumber = PhoneNumber.of(raw);
            return this;
        }

        public SubscriptionBuilder state(SubscriptionState state) {
            this.state = state;
            return this;
        }

        public Subscription build() {
            return Subscription.of(phoneNumber, state);
        }
    }
}
