package com.artinus.history.repository;

import com.artinus.history.domain.SubscriptionEvent;
import com.artinus.subscription.domain.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionEventRepository extends JpaRepository<SubscriptionEvent, Long> {

    List<SubscriptionEvent> findByPhoneNumberOrderByOccurredAtDesc(PhoneNumber phoneNumber);
}
