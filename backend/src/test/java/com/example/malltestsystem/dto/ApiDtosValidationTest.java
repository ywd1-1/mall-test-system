package com.example.malltestsystem.dto;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiDtosValidationTest {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void fractionalStockIsRejectedForProductSaveAndInlineUpdate() {
        ApiDtos.ProductRequest productRequest = new ApiDtos.ProductRequest();
        productRequest.setName("测试商品");
        productRequest.setPrice(new BigDecimal("10.00"));
        productRequest.setStock(new BigDecimal("1.5"));
        productRequest.setCategory("配件");

        ApiDtos.StockRequest stockRequest = new ApiDtos.StockRequest();
        stockRequest.setStock(new BigDecimal("1.5"));

        assertHasViolation(validator.validate(productRequest), "stock", "库存必须为非负整数");
        assertHasViolation(validator.validate(stockRequest), "stock", "库存必须为非负整数");
    }

    @Test
    void emptyCartSelectionIsRejected() {
        ApiDtos.CreateOrderRequest request = new ApiDtos.CreateOrderRequest();
        request.setAddressId(1L);
        request.setCartIds(Collections.emptyList());

        assertHasViolation(validator.validate(request), "cartIds", "请选择要结算的商品");
    }

    private static void assertHasViolation(Set<? extends ConstraintViolation<?>> violations,
                                           String property,
                                           String message) {
        assertTrue(violations.stream().anyMatch(violation ->
                        property.equals(violation.getPropertyPath().toString())
                                && message.equals(violation.getMessage())),
                "Expected validation error on " + property + ": " + message);
    }
}
