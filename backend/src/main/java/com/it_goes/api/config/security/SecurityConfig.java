package com.it_goes.api.config.security;

import com.it_goes.api.config.security.jwt.AuthEntryPointJwt;
import com.it_goes.api.config.security.jwt.AuthTokenFilter;
import com.it_goes.api.config.security.user.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/auth/**", "/health").permitAll()
                                .requestMatchers("/test/**").permitAll()
                                .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

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