package com.debbiecyber.QuickBite.repository;

import com.debbiecyber.QuickBite.entity.SavedAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedAddressRepository extends JpaRepository<SavedAddress, Long> {
    List<SavedAddress> findByCustomerIdOrderByIsDefaultDescCreatedAtDesc(Long customerId);
}
