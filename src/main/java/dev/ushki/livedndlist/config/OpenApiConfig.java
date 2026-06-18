package dev.ushki.livedndlist.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Live D&D List Application API")
            .version("0.0.1")
            .description("REST API for managing, creating and sharing your D&D character sheets")
            .contact(new Contact()
                .name("Timofey Tomashevski")
                .email("timtk.work@gmail.com")
                .url("https://github.com/Margit-The-Fell-Omenm"))
            .license(new License()
                .name("Unlicensed")
                .url("https://unlicense.org/")))
        .servers(List.of(
            new Server().url("http://localhost:8081").description("Dev сервер"),
            new Server().url("http://live-dnd-list.duckdns.org").description("Prod сервер")
        ));
  }
}
