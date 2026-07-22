package com.example.malltestsystem.service;

import com.example.malltestsystem.common.BusinessException;
import com.example.malltestsystem.common.UserContext;
import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.entity.OrderEntity;
import com.example.malltestsystem.entity.Product;
import com.example.malltestsystem.entity.User;
import com.example.malltestsystem.repository.OrderRepository;
import com.example.malltestsystem.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatisticsService {
    private static final int LOW_STOCK_THRESHOLD = 20;

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public StatisticsService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public ApiDtos.StatisticsResponse getCurrentStatistics() {
        User user = UserContext.get();
        if (user == null) {
            throw BusinessException.unauthorized("未登录");
        }

        if (User.ROLE_ADMIN.equals(user.getRole())) {
            return new ApiDtos.StatisticsResponse(
                    productRepository.countByDeletedFalse(),
                    productRepository.countByDeletedFalseAndStockLessThanEqual(LOW_STOCK_THRESHOLD),
                    orderRepository.count(),
                    orderRepository.countByStatus(OrderEntity.STATUS_CREATED)
            );
        }

        return new ApiDtos.StatisticsResponse(
                productRepository.countByDeletedFalseAndStatus(Product.STATUS_ON_SALE),
                productRepository.countByDeletedFalseAndStatusAndStockLessThanEqual(Product.STATUS_ON_SALE, LOW_STOCK_THRESHOLD),
                orderRepository.countByUserId(user.getId()),
                orderRepository.countByUserIdAndStatus(user.getId(), OrderEntity.STATUS_CREATED)
        );
    }
}
