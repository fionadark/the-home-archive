package com.homearchive.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security configuration for the Home Archive application.
 * Provides comprehensive security configuration with CORS support and security headers.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final CorsConfigurationSource corsConfigurationSource;
    
    @Autowired
    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS with custom configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // Disable CSRF for API endpoints (stateless)
            .csrf(csrf -> csrf.disable())
            
            // Configure session management (stateless for REST API)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Allow public access to search endpoints
                .requestMatchers("/api/books/**").permitAll()
                
                // Allow public access to health and metrics endpoints
                .requestMatchers("/api/actuator/health/**").permitAll()
                .requestMatchers("/api/actuator/info/**").permitAll()
                .requestMatchers("/api/actuator/metrics/**").permitAll()
                .requestMatchers("/api/actuator/database-performance/**").permitAll()
                .requestMatchers("/api/health/**").permitAll() // Custom health endpoints
                
                // Allow access to H2 console in development
                .requestMatchers("/h2-console/**").permitAll()
                
                // Allow access to Swagger/OpenAPI documentation (required by constitution)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // Allow OPTIONS requests for CORS preflight
                .requestMatchers("OPTIONS", "/**").permitAll()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Configure security headers (additional to our custom filter)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Allow H2 console frames
                .contentTypeOptions(contentTypeOptions -> {})
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                    .preload(true))
                .referrerPolicy(referrerPolicy -> 
                    referrerPolicy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                // X-Content-Type-Options is handled by our custom filter
                // X-Frame-Options is handled by our custom filter  
                // X-XSS-Protection is handled by our custom filter
            );
        
        return http.build();
    }
}