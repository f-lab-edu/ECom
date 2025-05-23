package com.example.api.module.shipping_address.service;

import com.example.api.module.shipping_address.controller.request.ShippingAddressRequest;
import com.example.api.module.shipping_address.controller.response.ShippingAddressResponse;
import com.example.core.domain.shipping_address.ShippingAddress;
import com.example.core.domain.shipping_address.api.ShippingAddressApiRepository;
import com.example.core.domain.user.User;
import com.example.core.domain.user.api.UserApiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingAddressService {

    private final ShippingAddressApiRepository shippingAddressApiRepository;
    private final UserApiRepository userApiRepository;

    @Transactional(readOnly = true)
    public List<ShippingAddressResponse> getShippingAddresses(Long userId) {
        return shippingAddressApiRepository.findAllByUserId(userId).stream()
                .map(ShippingAddressResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShippingAddressResponse createShippingAddress(Long userId, ShippingAddressRequest req) {
        User user = userApiRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Long count = shippingAddressApiRepository.countByUserId(userId);

        ShippingAddress shippingAddress = ShippingAddress.builder()
                .recipientName(req.getRecipientName())
                .address(req.getAddress())
                .zipCode(req.getZipCode())
                .phoneNumber(req.getPhoneNumber())
                .isDefault(count == 0)
                .user(user)
                .build();

        return ShippingAddressResponse.from(shippingAddressApiRepository.save(shippingAddress));
    }

    @Transactional
    public ShippingAddressResponse updateShippingAddress(Long userId, Long addressId, ShippingAddressRequest req) {
        ShippingAddress shippingAddress = shippingAddressApiRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Shipping address not found"));

        shippingAddress.setRecipientName(req.getRecipientName());
        shippingAddress.setAddress(req.getAddress());
        shippingAddress.setZipCode(req.getZipCode());
        shippingAddress.setPhoneNumber(req.getPhoneNumber());

        shippingAddressApiRepository.save(shippingAddress);

        return ShippingAddressResponse.from(shippingAddress);
    }

    @Transactional
    public ShippingAddressResponse setDefaultShippingAddress(Long userId, Long addressId) {
        ShippingAddress shippingAddress = shippingAddressApiRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Shipping address not found"));

        List<ShippingAddress> userAddresses = shippingAddressApiRepository.findAllByUserId(userId);
        userAddresses.forEach(address -> address.setIsDefault(false));
        shippingAddress.setIsDefault(true);

        return ShippingAddressResponse.from(shippingAddress);
    }

    @Transactional
    public void deleteShippingAddress(Long userId, Long addressId) {
        ShippingAddress shippingAddress = shippingAddressApiRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Shipping address not found"));

        shippingAddressApiRepository.delete(shippingAddress);
    }


}
