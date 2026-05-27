package com.onepiecerpg.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {
  
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
      .info(new Info()
        .title("One Piece RPG API")
        .version("1.0")
        .description("Documentation de l'API pour le projet One Piece RPG"))
      .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
      .components(new Components()
        .addSecuritySchemes("bearerAuth", new SecurityScheme()
          .name("bearerAuth")
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")
        )
      );
  }
}
