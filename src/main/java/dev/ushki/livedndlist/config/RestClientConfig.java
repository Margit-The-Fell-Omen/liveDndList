package dev.ushki.livedndlist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  public RestClient restClient() {
    return RestClient.builder()
        .baseUrl("https://api.open5e.com/v1")
        .build();
  }
}
