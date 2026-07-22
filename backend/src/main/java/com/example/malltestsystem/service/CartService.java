package com.example.malltestsystem.service;

import com.example.malltestsystem.common.BusinessException;
import com.example.malltestsystem.common.UserContext;
import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.entity.Cart;
import com.example.malltestsystem.entity.Product;
import com.example.malltestsystem.entity.User;
import com.example.malltestsystem.repository.CartRepository;
import com.example.malltestsystem.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public ApiDtos.CartItemResponse addToCart(ApiDtos.AddCartRequest request) {
        User user = requireLoginUser();
        Product product = getOnSaleProduct(request.getProductId());

        Cart cart = cartRepository.findByUserIdAndProductId(user.getId(), product.getId()).orElse(null);
        int quantity = request.getQuantity();
        if (cart != null) {
            quantity += cart.getQuantity();
        }
        assertStockEnough(product, quantity);

        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setProduct(product);
            cart.setCreatedAt(LocalDateTime.now());
        }
        cart.setQuantity(quantity);
        cart.setUpdatedAt(LocalDateTime.now());
        return toCartItemResponse(cartRepository.save(cart));
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.CartItemResponse> listCart() {
        User user = requireLoginUser();
        List<Cart> carts = cartRepository.findByUserIdOrderByIdDesc(user.getId());
        List<ApiDtos.CartItemResponse> responses = new ArrayList<ApiDtos.CartItemResponse>();
        for (Cart cart : carts) {
            responses.add(toCartItemResponse(cart));
        }
        return responses;
    }

    @Transactional
    public ApiDtos.CartItemResponse updateCart(Long id, ApiDtos.UpdateCartRequest request) {
        User user = requireLoginUser();
        Cart cart = cartRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> BusinessException.notFound("购物车商品不存在"));
        assertStockEnough(cart.getProduct(), request.getQuantity());
        cart.setQuantity(request.getQuantity());
        cart.setUpdatedAt(LocalDateTime.now());
        return toCartItemResponse(cartRepository.save(cart));
    }

    @Transactional
    public void deleteCart(Long id) {
        User user = requireLoginUser();
        Cart cart = cartRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> BusinessException.notFound("购物车商品不存在"));
        cartRepository.delete(cart);
    }

    private Product getOnSaleProduct(Long productId) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> BusinessException.notFound("商品不存在"));
        if (!Product.STATUS_ON_SALE.equals(product.getStatus())) {
            throw BusinessException.badRequest("商品已下架，不能加入购物车");
        }
        return product;
    }

    private void assertStockEnough(Product product, int quantity) {
        if (quantity <= 0) {
            throw BusinessException.badRequest("商品数量必须大于 0");
        }
        if (product.getStock() < quantity) {
            throw BusinessException.badRequest("商品库存不足");
        }
    }

    private User requireLoginUser() {
        User user = UserContext.get();
        if (user == null) {
            throw BusinessException.unauthorized("未登录");
        }
        return user;
    }

    private ApiDtos.CartItemResponse toCartItemResponse(Cart cart) {
        Product product = cart.getProduct();
        ApiDtos.CartItemResponse response = new ApiDtos.CartItemResponse();
        response.setId(cart.getId());
        response.setProductId(product.getId());
        response.setProductName(product.getName());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setQuantity(cart.getQuantity());
        response.setProductStatus(product.getStatus());
        response.setImageUrl(product.getImageUrl());
        response.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        return response;
    }
}
