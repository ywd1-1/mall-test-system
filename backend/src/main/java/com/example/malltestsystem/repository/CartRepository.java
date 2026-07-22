package com.example.malltestsystem.repository;

import com.example.malltestsystem.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserIdOrderByIdDesc(Long userId);

    Optional<Cart> findByUserIdAndProductId(Long userId, Long productId);

    Optional<Cart> findByIdAndUserId(Long id, Long userId);

    List<Cart> findByUserIdAndIdIn(Long userId, Collection<Long> ids);

    void deleteByUserIdAndIdIn(Long userId, Collection<Long> ids);

    void deleteByProductId(Long productId);
}
