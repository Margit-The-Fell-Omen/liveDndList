package dev.ushki.livedndlist.config;

import java.net.http.HttpClient;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@ConfigurationProperties(prefix = "open5e.api")
@Getter
@Setter
public class Open5eApiConfig {

  private String baseUrl = "https://api.open5e.com/";

  @Bean
  public RestClient open5eRestClient() {
    HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(Duration.ofSeconds(30));

    return RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(requestFactory)
        .defaultHeader("User-Agent", "LiveDndList/1.0 (Spring Boot; Open5e Consumer)")
        .defaultHeader("Accept", "application/json")
        .requestInterceptor((request, body, execution) -> {
          var response = execution.execute(request, body);
          return response;
        })
        .build();
  }
}
