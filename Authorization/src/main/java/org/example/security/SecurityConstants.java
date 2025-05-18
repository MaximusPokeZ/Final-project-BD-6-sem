package org.example.security;

import java.util.List;

public class SecurityConstants {
    public static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/register",
            "/swagger-ui/**",
            "/swagger-resources/*",
            "/v3/api-docs/**",
            "/actuator/**"
    );

    private SecurityConstants() {
    }
}
