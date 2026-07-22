package com.example.malltestsystem.repository;

import com.example.malltestsystem.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {
    List<OrderEntity> findByUserIdOrderByIdDesc(Long userId);

    List<OrderEntity> findByUserIdAndStatusOrderByIdDesc(Long userId, String status);

    List<OrderEntity> findAllByOrderByIdDesc();

    Optional<OrderEntity> findByIdAndUserId(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderEntity o where o.id = :id and o.user.id = :userId")
    Optional<OrderEntity> findByIdAndUserIdForUpdate(@Param("id") Long id, @Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderEntity o where o.id = :id")
    Optional<OrderEntity> findByIdForUpdate(@Param("id") Long id);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, String status);

    long countByStatus(String status);

}
