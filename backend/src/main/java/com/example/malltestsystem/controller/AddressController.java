package com.example.malltestsystem.controller;

import com.example.malltestsystem.common.ApiResponse;
import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.service.AddressService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@Tag(name = "收货地址")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ApiResponse<List<ApiDtos.AddressResponse>> listAddresses() {
        return ApiResponse.success(addressService.listCurrentUserAddresses());
    }

    @PostMapping
    public ApiResponse<ApiDtos.AddressResponse> createAddress(@Valid @RequestBody ApiDtos.AddressRequest request) {
        return ApiResponse.success(addressService.createAddress(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ApiDtos.AddressResponse> updateAddress(@PathVariable Long id,
                                                              @Valid @RequestBody ApiDtos.AddressRequest request) {
        return ApiResponse.success(addressService.updateAddress(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/default")
    public ApiResponse<ApiDtos.AddressResponse> setDefaultAddress(@PathVariable Long id) {
        return ApiResponse.success(addressService.setDefaultAddress(id));
    }
}
