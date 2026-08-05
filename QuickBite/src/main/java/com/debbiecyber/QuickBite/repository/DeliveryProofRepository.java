package com.debbiecyber.QuickBite.repository;

import com.debbiecyber.QuickBite.entity.DeliveryProof;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeliveryProofRepository extends JpaRepository<DeliveryProof, Long> {
    boolean existsByOrderId(Long orderId);
    Optional<DeliveryProof> findByOrderId(Long orderId);
}
