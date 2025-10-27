package io.github.minjoon98.bookmark.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bookmark API")
                        .description("개인 북마크 관리 REST API")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("minjoon-98")
                                .url("https://github.com/minjoon-98/bookmark-api")));
    }
}
