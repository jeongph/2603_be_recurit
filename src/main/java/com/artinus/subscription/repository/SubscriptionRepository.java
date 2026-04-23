package com.artinus.subscription.repository;

import com.artinus.subscription.domain.PhoneNumber;
import com.artinus.subscription.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, PhoneNumber> {

    default Optional<Subscription> findByPhoneNumber(PhoneNumber phoneNumber) {
        return findById(phoneNumber);
    }
}
