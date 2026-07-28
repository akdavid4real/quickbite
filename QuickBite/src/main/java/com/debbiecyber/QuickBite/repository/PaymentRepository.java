package com.debbiecyber.QuickBite.repository;


import com.debbiecyber.QuickBite.entity.Payment;
import com.debbiecyber.QuickBite.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReference(String reference);

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(PaymentStatus paymentStatus);

}
