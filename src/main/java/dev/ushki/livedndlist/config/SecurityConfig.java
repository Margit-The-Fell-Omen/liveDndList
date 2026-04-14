package dev.ushki.livedndlist.config;

import dev.ushki.livedndlist.security.jwt.JwtAuthenticationEntryPoint;
import dev.ushki.livedndlist.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  public SecurityConfig(
      @Lazy JwtAuthenticationFilter jwtAuthenticationFilter,
      JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  // NOTE: The WebSecurityCustomizer bean has been removed to avoid confusion.
  // All rules are now centralized in the SecurityFilterChain.

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(jwtAuthenticationEntryPoint))
        .sessionManagement(sm -> sm
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth
            // FIX: Create one comprehensive list of all public endpoints.
            .requestMatchers(
                // --- Authentication ---
                "/api/v1/auth/**",

                // --- All Swagger/OpenAPI related paths ---
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**", // The default path for the API specification JSON
                "/swagger-resources/**", // Another common path for swagger resources
                "/api-docs/**" // A common custom path, included for safety
            ).permitAll()

            // After permitting the public paths, require authentication for all other /api/v1/ endpoints.
            .requestMatchers("/api/v1/**").authenticated()

            // For any other request not matched above, deny it. This is a secure default.
            .anyRequest().denyAll()
        );

    return http.build();
  }
}
