package com.it_goes.api.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class WriteOriginFilter extends OncePerRequestFilter {
    private final Set<String> allowedOrigins;

    public WriteOriginFilter(String origins) {
        allowedOrigins = Arrays.stream(origins.split(",")).map(String::trim).collect(Collectors.toSet());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String method = request.getMethod();
        final String path = request.getRequestURI().substring(request.getContextPath().length());
        final boolean newWrite = (path.equals("/me/ski-days") || path.startsWith("/me/ski-days/") || path.equals("/locations") || path.equals("/trips"))
                && !method.equals("GET") && !method.equals("OPTIONS");
        final String origin = request.getHeader("Origin");
        if (newWrite && (origin == null || !allowedOrigins.contains(origin))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Request origin is not allowed.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
