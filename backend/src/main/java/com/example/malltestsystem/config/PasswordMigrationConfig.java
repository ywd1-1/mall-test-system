package com.example.malltestsystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PasswordMigrationConfig implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(PasswordMigrationConfig.class);

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final String bootstrapAdminPassword;

    public PasswordMigrationConfig(JdbcTemplate jdbcTemplate,
                                   PasswordEncoder passwordEncoder,
                                   @Value("${app.bootstrap-admin-password:}") String bootstrapAdminPassword) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapAdminPassword = bootstrapAdminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!userTableExists()) {
            return;
        }
        migrateLegacyPasswords();
        applyBootstrapAdminPassword();
    }

    private void migrateLegacyPasswords() {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("select id, password from `user`");
        int migrated = 0;
        for (Map<String, Object> user : users) {
            Number id = (Number) user.get("id");
            String password = (String) user.get("password");
            if (id != null && password != null && !isBcrypt(password)) {
                migrated += jdbcTemplate.update(
                        "update `user` set password = ?, updated_at = now() where id = ? and password = ?",
                        passwordEncoder.encode(password), id.longValue(), password);
            }
        }
        if (migrated > 0) {
            log.info("Migrated {} legacy user passwords to BCrypt", migrated);
        }
    }

    private void applyBootstrapAdminPassword() {
        if (bootstrapAdminPassword == null || bootstrapAdminPassword.trim().isEmpty()) {
            return;
        }
        List<String> passwords = jdbcTemplate.query(
                "select password from `user` where username = 'admin' and role = 'ADMIN'",
                (resultSet, rowNum) -> resultSet.getString("password"));
        if (passwords.isEmpty() || passwordEncoder.matches(bootstrapAdminPassword, passwords.get(0))) {
            return;
        }
        jdbcTemplate.update(
                "update `user` set password = ?, updated_at = now() where username = 'admin' and role = 'ADMIN'",
                passwordEncoder.encode(bootstrapAdminPassword));
        log.info("Applied the configured bootstrap administrator password");
    }

    private boolean userTableExists() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_schema = database() and table_name = 'user'",
                Integer.class);
        return count != null && count > 0;
    }

    private boolean isBcrypt(String password) {
        return password.matches("^\\$2[ayb]\\$\\d{2}\\$.{53}$");
    }
}
