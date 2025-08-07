package com.example.api.module.shipping_address.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.module.shipping_address.controller.request.ShippingAddressRequest;
import com.example.api.module.shipping_address.controller.response.ShippingAddressResponse;
import com.example.api.module.shipping_address.service.ShippingAddressService;
import com.example.core.model.response.DataResponse;
import com.example.core.model.response.ResponseCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shipping-address")
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public DataResponse<List<ShippingAddressResponse>> getShippingAddresses(
            @AuthenticationPrincipal Long userId) {
        return DataResponse.of(shippingAddressService.getShippingAddresses(userId));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public DataResponse<ShippingAddressResponse> createShippingAddress(
            @AuthenticationPrincipal Long userId,
            @RequestBody ShippingAddressRequest request) {
        return DataResponse.of(shippingAddressService.createShippingAddress(userId, request));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{addressId}")
    public DataResponse<ShippingAddressResponse> updateShippingAddress(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long addressId,
            @RequestBody ShippingAddressRequest request) {
        return DataResponse.of(shippingAddressService.updateShippingAddress(userId, addressId, request));
    }

    @PutMapping("/{addressId}/default")
    public DataResponse<ShippingAddressResponse> setDefaultShippingAddress(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long addressId) {
        return DataResponse.of(shippingAddressService.setDefaultShippingAddress(userId, addressId));
    }

    @DeleteMapping("/{addressId}")
    public DataResponse<Void> deleteShippingAddress(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long addressId) {
        shippingAddressService.deleteShippingAddress(userId, addressId);
        return DataResponse.of(ResponseCode.SUCCESS, null);
    }
}
