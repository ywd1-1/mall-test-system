package com.example.malltestsystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI mallTestSystemOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("mall-test-system V2 API")
                        .version("2.0.0")
                        .description("中等复杂度电商订单管理系统接口，统一响应格式为 code/message/data。"))
                .components(new Components().addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("Token")));
    }
}
