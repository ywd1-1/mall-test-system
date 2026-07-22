package com.example.malltestsystem.repository;

import com.example.malltestsystem.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByStatusOrderByIdDesc(String status);

    long countByStatus(String status);

    long countByStockLessThanEqual(Integer stock);

    long countByStatusAndStockLessThanEqual(String status, Integer stock);

    long countByDeletedFalse();

    long countByDeletedFalseAndStatus(String status);

    long countByDeletedFalseAndStockLessThanEqual(Integer stock);

    long countByDeletedFalseAndStatusAndStockLessThanEqual(String status, Integer stock);

    Optional<Product> findByIdAndDeletedFalse(Long id);

    @Query("select p from Product p order by p.id desc")
    List<Product> findAllOrderByIdDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
