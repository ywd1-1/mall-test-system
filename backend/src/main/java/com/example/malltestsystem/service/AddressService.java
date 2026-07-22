package com.example.malltestsystem.service;

import com.example.malltestsystem.common.BusinessException;
import com.example.malltestsystem.common.UserContext;
import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.entity.User;
import com.example.malltestsystem.entity.UserAddress;
import com.example.malltestsystem.repository.UserAddressRepository;
import com.example.malltestsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AddressService {
    private final UserAddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(UserAddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.AddressResponse> listCurrentUserAddresses() {
        User user = requireUser();
        List<ApiDtos.AddressResponse> responses = new ArrayList<ApiDtos.AddressResponse>();
        for (UserAddress address : addressRepository.findByUserIdOrderByIsDefaultDescIdDesc(user.getId())) {
            responses.add(toResponse(address));
        }
        return responses;
    }

    @Transactional
    public ApiDtos.AddressResponse createAddress(ApiDtos.AddressRequest request) {
        User user = lockCurrentUser();
        boolean makeDefault = Boolean.TRUE.equals(request.getIsDefault()) || addressRepository.countByUserId(user.getId()) == 0;
        if (makeDefault) {
            addressRepository.clearDefaultByUserId(user.getId());
        }

        LocalDateTime now = LocalDateTime.now();
        UserAddress address = new UserAddress();
        address.setUser(user);
        applyRequest(address, request);
        address.setIsDefault(makeDefault);
        address.setCreatedAt(now);
        address.setUpdatedAt(now);
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public ApiDtos.AddressResponse updateAddress(Long id, ApiDtos.AddressRequest request) {
        User user = lockCurrentUser();
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultByUserId(user.getId());
        }
        UserAddress address = getOwnedAddress(id, user.getId());
        applyRequest(address, request);
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        address.setUpdatedAt(LocalDateTime.now());
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long id) {
        User user = lockCurrentUser();
        UserAddress address = getOwnedAddress(id, user.getId());
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        addressRepository.delete(address);
        addressRepository.flush();
        if (wasDefault) {
            addressRepository.findFirstByUserIdOrderByIsDefaultDescIdDesc(user.getId()).ifPresent(next -> {
                next.setIsDefault(true);
                next.setUpdatedAt(LocalDateTime.now());
                addressRepository.save(next);
            });
        }
    }

    @Transactional
    public ApiDtos.AddressResponse setDefaultAddress(Long id) {
        User user = lockCurrentUser();
        addressRepository.clearDefaultByUserId(user.getId());
        UserAddress address = getOwnedAddress(id, user.getId());
        address.setIsDefault(true);
        address.setUpdatedAt(LocalDateTime.now());
        return toResponse(addressRepository.save(address));
    }

    private User lockCurrentUser() {
        User current = requireUser();
        return userRepository.findByIdForUpdate(current.getId())
                .orElseThrow(() -> BusinessException.unauthorized("登录用户不存在"));
    }

    private User requireUser() {
        User user = UserContext.get();
        if (user == null) {
            throw BusinessException.unauthorized("未登录");
        }
        if (!User.ROLE_USER.equals(user.getRole())) {
            throw BusinessException.forbidden("管理员不能维护普通用户收货地址");
        }
        return user;
    }

    private UserAddress getOwnedAddress(Long id, Long userId) {
        return addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> BusinessException.notFound("收货地址不存在"));
    }

    private void applyRequest(UserAddress address, ApiDtos.AddressRequest request) {
        address.setRecipientName(request.getRecipientName().trim());
        address.setPhone(request.getPhone().trim());
        address.setProvince(request.getProvince().trim());
        address.setCity(request.getCity().trim());
        address.setDistrict(request.getDistrict().trim());
        address.setDetailAddress(request.getDetailAddress().trim());
    }

    public ApiDtos.AddressResponse toResponse(UserAddress address) {
        ApiDtos.AddressResponse response = new ApiDtos.AddressResponse();
        response.setId(address.getId());
        response.setRecipientName(address.getRecipientName());
        response.setPhone(address.getPhone());
        response.setProvince(address.getProvince());
        response.setCity(address.getCity());
        response.setDistrict(address.getDistrict());
        response.setDetailAddress(address.getDetailAddress());
        response.setIsDefault(address.getIsDefault());
        response.setCreatedAt(address.getCreatedAt());
        response.setUpdatedAt(address.getUpdatedAt());
        return response;
    }
}
