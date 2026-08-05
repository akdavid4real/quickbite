package com.debbiecyber.QuickBite.service;

import com.debbiecyber.QuickBite.dto.resquest.ProfileRequest;
import com.debbiecyber.QuickBite.dto.resquest.SavedAddressRequest;
import com.debbiecyber.QuickBite.dto.response.SavedAddressResponse;
import com.debbiecyber.QuickBite.dto.response.UserResponse;
import com.debbiecyber.QuickBite.entity.SavedAddress;
import com.debbiecyber.QuickBite.entity.User;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.exceptions.BadRequestException;
import com.debbiecyber.QuickBite.exceptions.ResourceNotFoundException;
import com.debbiecyber.QuickBite.repository.SavedAddressRepository;
import com.debbiecyber.QuickBite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {
    private final UserRepository userRepository;
    private final SavedAddressRepository addressRepository;

    public UserResponse getProfile(String email) {
        return mapUser(getUser(email));
    }

    @Transactional
    public UserResponse updateProfile(String email, ProfileRequest request) {
        User user = getUser(email);
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        return mapUser(userRepository.save(user));
    }

    public List<SavedAddressResponse> getAddresses(String email) {
        User customer = getCustomer(email);
        return addressRepository.findByCustomerIdOrderByIsDefaultDescCreatedAtDesc(customer.getId())
                .stream().map(this::mapAddress).toList();
    }

    @Transactional
    public SavedAddressResponse createAddress(String email, SavedAddressRequest request) {
        User customer = getCustomer(email);
        List<SavedAddress> existing = addressRepository.findByCustomerIdOrderByIsDefaultDescCreatedAtDesc(customer.getId());
        boolean makeDefault = request.isDefault() || existing.isEmpty();
        if (makeDefault) clearDefaults(existing);

        SavedAddress address = SavedAddress.builder()
                .customer(customer)
                .label(request.getLabel())
                .address(request.getAddress())
                .isDefault(makeDefault)
                .build();
        return mapAddress(addressRepository.save(address));
    }

    @Transactional
    public SavedAddressResponse updateAddress(Long addressId, String email, SavedAddressRequest request) {
        User customer = getCustomer(email);
        SavedAddress address = getOwnedAddress(addressId, customer);
        if (request.isDefault()) {
            clearDefaults(addressRepository.findByCustomerIdOrderByIsDefaultDescCreatedAtDesc(customer.getId()));
        }
        address.setLabel(request.getLabel());
        address.setAddress(request.getAddress());
        address.setIsDefault(request.isDefault() || Boolean.TRUE.equals(address.getIsDefault()));
        return mapAddress(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long addressId, String email) {
        User customer = getCustomer(email);
        SavedAddress address = getOwnedAddress(addressId, customer);
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        addressRepository.delete(address);
        if (wasDefault) {
            List<SavedAddress> remaining = addressRepository.findByCustomerIdOrderByIsDefaultDescCreatedAtDesc(customer.getId());
            if (!remaining.isEmpty()) {
                remaining.get(0).setIsDefault(true);
                addressRepository.save(remaining.get(0));
            }
        }
    }

    private void clearDefaults(List<SavedAddress> addresses) {
        addresses.forEach(address -> address.setIsDefault(false));
        addressRepository.saveAll(addresses);
    }

    private SavedAddress getOwnedAddress(Long id, User customer) {
        SavedAddress address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Saved address not found"));
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new ResourceNotFoundException("Saved address not found");
        }
        return address;
    }

    private User getCustomer(String email) {
        User user = getUser(email);
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new BadRequestException("Saved addresses are only available to customers");
        }
        return user;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponse mapUser(User user) {
        return UserResponse.builder()
                .id(user.getId()).name(user.getName()).email(user.getEmail())
                .phoneNumber(user.getPhoneNumber()).role(user.getRole()).address(user.getAddress())
                .accountStatus(user.getAccountStatus()).verificationStatus(user.getVerificationStatus())
                .availableForDelivery(user.getAvailableForDelivery()).createdAt(user.getCreatedAt())
                .build();
    }

    private SavedAddressResponse mapAddress(SavedAddress address) {
        return SavedAddressResponse.builder().id(address.getId()).label(address.getLabel())
                .address(address.getAddress()).isDefault(address.getIsDefault()).build();
    }
}
