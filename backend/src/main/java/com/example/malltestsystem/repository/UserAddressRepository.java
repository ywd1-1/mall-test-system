package com.example.malltestsystem.repository;

import com.example.malltestsystem.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserIdOrderByIsDefaultDescIdDesc(Long userId);

    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);

    Optional<UserAddress> findFirstByUserIdOrderByIsDefaultDescIdDesc(Long userId);

    long countByUserId(Long userId);

    @Modifying(flushAutomatically = true)
    @Query("update UserAddress a set a.isDefault = false, a.updatedAt = CURRENT_TIMESTAMP where a.user.id = :userId and a.isDefault = true")
    int clearDefaultByUserId(@Param("userId") Long userId);
}
