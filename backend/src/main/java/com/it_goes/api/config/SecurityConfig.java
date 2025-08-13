package com.it_goes.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
            .csrf(csrf -> {
                try { // Not sure why I need this try catch, IDE's made me add...
                    csrf.disable()
                    .cors(Customizer.withDefaults())
                    .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll() // CORS preflight
                    .anyRequest().permitAll()); // no auth required anywhere
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        // DEV:  CORS_ALLOWED_ORIGINS=http://localhost:3000
        // PROD: CORS_ALLOWED_ORIGINS=https://my-frontend.com
        final String originsEnv = System.getenv().getOrDefault("CORS_ALLOWED_ORIGINS", "http://localhost:3000");
        final List<String> allowedOrigins = Arrays.stream(originsEnv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins); // Set which domain(s) can call my API
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS")); // Allowed methods
        config.setAllowedHeaders(List.of("Content-Type","Authorization","X-Requested-With")); // Allowed headers
        config.setExposedHeaders(List.of("Location")); // headers the browser is allowed to read from the response.
        config.setAllowCredentials(true); // fine even without auth; keep origins strict
        config.setMaxAge(3600L); // Browser can cache preflight options for one hour

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config); // Apply to all endpoint
        return source;
    }
}