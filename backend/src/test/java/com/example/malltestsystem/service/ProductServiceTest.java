package com.example.malltestsystem.service;

import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.entity.Product;
import com.example.malltestsystem.repository.CartRepository;
import com.example.malltestsystem.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductServiceTest {
    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        productService = new ProductService(productRepository, cartRepository);

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(100L);
            return product;
        });
    }

    @Test
    void createProductUsesTestImageWhenImageUrlIsMissing() {
        ApiDtos.ProductRequest request = productRequest(null);

        ApiDtos.ProductResponse response = productService.createProduct(request);

        assertEquals("/product-images/test-product.webp", response.getImageUrl());
    }

    @Test
    void createProductKeepsProvidedImageUrl() {
        ApiDtos.ProductRequest request = productRequest(" /product-images/phone-x1.webp ");

        ApiDtos.ProductResponse response = productService.createProduct(request);

        assertEquals("/product-images/phone-x1.webp", response.getImageUrl());
    }

    private ApiDtos.ProductRequest productRequest(String imageUrl) {
        ApiDtos.ProductRequest request = new ApiDtos.ProductRequest();
        request.setName("自动化测试商品");
        request.setPrice(new BigDecimal("99.00"));
        request.setStock(BigDecimal.TEN);
        request.setCategory("配件");
        request.setStatus(Product.STATUS_ON_SALE);
        request.setImageUrl(imageUrl);
        request.setDescription("用于验证商品默认图片");
        return request;
    }
}
