package com.example.malltestsystem.service;

import com.example.malltestsystem.common.BusinessException;
import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.entity.Product;
import com.example.malltestsystem.repository.CartRepository;
import com.example.malltestsystem.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    private static final String DEFAULT_PRODUCT_IMAGE_URL = "/product-images/test-product.webp";

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    public ProductService(ProductRepository productRepository, CartRepository cartRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
    }

    @Transactional(readOnly = true)
    public ApiDtos.PageResponse<ApiDtos.ProductResponse> listOnSaleProducts(String keyword,
                                                                            String category,
                                                                            int page,
                                                                            int size) {
        Page<Product> products = productRepository.findAll(
                buildProductSpecification(keyword, category, Product.STATUS_ON_SALE),
                buildPageable(page, size)
        );
        return toPageResponse(products);
    }

    @Transactional(readOnly = true)
    public ApiDtos.PageResponse<ApiDtos.ProductResponse> listProductsForAdmin(String keyword,
                                                                              String category,
                                                                              String status,
                                                                              int page,
                                                                              int size) {
        Page<Product> products = productRepository.findAll(
                buildProductSpecification(keyword, category, normalizeQueryStatus(status)),
                buildPageable(page, size)
        );
        return toPageResponse(products);
    }

    @Transactional(readOnly = true)
    public ApiDtos.ProductResponse getProduct(Long id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> BusinessException.notFound("商品不存在"));
        if (!Product.STATUS_ON_SALE.equals(product.getStatus())) {
            throw BusinessException.notFound("商品不存在或已下架");
        }
        return toProductResponse(product);
    }

    @Transactional
    public ApiDtos.ProductResponse createProduct(ApiDtos.ProductRequest request) {
        Product product = new Product();
        applyProductRequest(product, request);
        product.setDeleted(false);
        product.setDeletedAt(null);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return toProductResponse(productRepository.save(product));
    }

    @Transactional
    public ApiDtos.ProductResponse updateProduct(Long id, ApiDtos.ProductRequest request) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> BusinessException.notFound("商品不存在"));
        applyProductRequest(product, request);
        product.setUpdatedAt(LocalDateTime.now());
        return toProductResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> BusinessException.notFound("商品不存在"));
        product.setStatus(Product.STATUS_OFF_SHELF);
        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        product.setUpdatedAt(product.getDeletedAt());
        cartRepository.deleteByProductId(id);
        productRepository.save(product);
    }

    @Transactional
    public ApiDtos.ProductResponse updateProductStatus(Long id, String status) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> BusinessException.notFound("商品不存在"));
        product.setStatus(normalizeStatus(status));
        product.setUpdatedAt(LocalDateTime.now());
        if (Product.STATUS_OFF_SHELF.equals(product.getStatus())) {
            cartRepository.deleteByProductId(id);
        }
        return toProductResponse(productRepository.save(product));
    }

    @Transactional
    public ApiDtos.ProductResponse updateProductStock(Long id, BigDecimal stock) {
        Integer normalizedStock = toStock(stock);
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> BusinessException.notFound("商品不存在"));
        product.setStock(normalizedStock);
        product.setUpdatedAt(LocalDateTime.now());
        return toProductResponse(productRepository.save(product));
    }

    private void applyProductRequest(Product product, ApiDtos.ProductRequest request) {
        product.setName(request.getName().trim());
        product.setPrice(request.getPrice());
        product.setStock(toStock(request.getStock()));
        product.setCategory(normalizeCategory(request.getCategory()));
        product.setStatus(normalizeStatus(request.getStatus()));
        String imageUrl = request.getImageUrl();
        product.setImageUrl(imageUrl == null || imageUrl.trim().isEmpty()
                ? DEFAULT_PRODUCT_IMAGE_URL
                : imageUrl.trim());
        product.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return Product.STATUS_ON_SALE;
        }
        if (Product.STATUS_ON_SALE.equals(status) || Product.STATUS_OFF_SHELF.equals(status)) {
            return status;
        }
        throw BusinessException.badRequest("商品状态只能是 ON_SALE 或 OFF_SHELF");
    }

    private Integer toStock(BigDecimal stock) {
        if (stock == null) {
            throw BusinessException.badRequest("库存不能为空");
        }
        try {
            int value = stock.intValueExact();
            if (value < 0) {
                throw BusinessException.badRequest("库存必须为非负整数");
            }
            return value;
        } catch (ArithmeticException ex) {
            throw BusinessException.badRequest("库存必须为非负整数");
        }
    }

    private String normalizeQueryStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        return normalizeStatus(status);
    }

    private String normalizeCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "未分类";
        }
        return category.trim();
    }

    private Pageable buildPageable(int page, int size) {
        if (page < 1) {
            throw BusinessException.badRequest("页码必须大于等于 1");
        }
        if (size < 1 || size > 100) {
            throw BusinessException.badRequest("每页数量必须在 1 到 100 之间");
        }
        return PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
    }

    private Specification<Product> buildProductSpecification(String keyword, String category, String status) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(builder.isFalse(root.<Boolean>get("deleted")));
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(builder.equal(root.get("status"), status.trim()));
            }
            if (category != null && !category.trim().isEmpty()) {
                predicates.add(builder.equal(root.get("category"), category.trim()));
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.<String>get("name")), pattern),
                        builder.like(builder.lower(root.<String>get("description")), pattern)
                ));
            }
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    private ApiDtos.PageResponse<ApiDtos.ProductResponse> toPageResponse(Page<Product> products) {
        return new ApiDtos.PageResponse<ApiDtos.ProductResponse>(
                toProductResponses(products.getContent()),
                products.getTotalElements(),
                products.getNumber() + 1,
                products.getSize()
        );
    }

    private List<ApiDtos.ProductResponse> toProductResponses(List<Product> products) {
        List<ApiDtos.ProductResponse> responses = new ArrayList<ApiDtos.ProductResponse>();
        for (Product product : products) {
            responses.add(toProductResponse(product));
        }
        return responses;
    }

    public ApiDtos.ProductResponse toProductResponse(Product product) {
        ApiDtos.ProductResponse response = new ApiDtos.ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setCategory(product.getCategory());
        response.setStatus(product.getStatus());
        response.setImageUrl(product.getImageUrl());
        response.setDescription(product.getDescription());
        return response;
    }
}
