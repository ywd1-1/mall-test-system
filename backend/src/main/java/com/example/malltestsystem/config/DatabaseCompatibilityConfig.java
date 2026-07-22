package com.example.malltestsystem.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DatabaseCompatibilityConfig {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseCompatibilityConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureProductCategoryColumn() {
        if (!productTableExists()) {
            return;
        }
        if (!productCategoryColumnExists()) {
            jdbcTemplate.execute("ALTER TABLE product ADD COLUMN category VARCHAR(50) NOT NULL DEFAULT '配件' AFTER stock");
        }
    }

    private boolean productTableExists() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_schema = database() and table_name = 'product'",
                Integer.class
        );
        return count != null && count > 0;
    }

    private boolean productCategoryColumnExists() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns " +
                        "where table_schema = database() and table_name = 'product' and column_name = 'category'",
                Integer.class
        );
        return count != null && count > 0;
    }
}
