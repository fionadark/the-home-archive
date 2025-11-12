package com.thehomearchive.library.config;

import com.thehomearchive.library.security.JwtAuthenticationEntryPoint;
import com.thehomearchive.library.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

/**
 * Spring Security Configuration for Dark Academia Library Application
 * 
 * Configures JWT-based authentication, authorization, CORS, and security policies.
 * Uses stateless session management with JWT tokens for API security.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private SecurityHeadersConfig securityHeadersConfig;

    /**
     * Password encoder bean using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength 12 for good security
    }

    /**
     * Authentication manager bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (configure for your frontend domains)
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:3000",  // React dev server
            "http://localhost:8080",  // Local development
            "https://*.thehomearchive.com", // Production domains
            "https://thehomearchive.com"
        ));
        
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Allow common headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Expose headers that client can access
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Authorization"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Main security filter chain configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API (using JWT tokens)
            .csrf(csrf -> csrf.disable())
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure session management (stateless)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure exception handling
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints (no authentication required)
                .requestMatchers(
                    "/auth/**",               // Authentication endpoints
                    "/dev/**",                // Development endpoints (dev profile only)
                    "/h2-console/**",         // H2 database console (dev only)
                    "/actuator/health",       // Health check
                    "/actuator/info",         // Application info
                    "/api-docs/**",           // OpenAPI documentation
                    "/swagger-ui/**",         // Swagger UI
                    "/swagger-ui.html",       // Swagger UI HTML
                    "/favicon.ico",           // Browser favicon
                    "/error",                 // Error pages
                    // Static resources
                    "/css/**",                // CSS files
                    "/js/**",                 // JavaScript files
                    "/images/**",             // Image files
                    "/fonts/**",              // Font files
                    "/static/**",             // Static resources
                    "/*.html",                // HTML pages (login, register, etc.)
                    "/dashboard.html",        // Dashboard page
                    "/login.html",            // Login page  
                    "/register.html",         // Registration page
                    "/*.css",                 // Root CSS files
                    "/*.js"                   // Root JavaScript files
                ).permitAll()
                
                // Admin-only endpoints
                .requestMatchers("/admin/**", "/v1/admin/**").hasRole("ADMIN")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            );

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        // Add security headers filter as the first filter in the chain
        http.addFilterBefore(securityHeadersConfig.securityHeadersFilter(), JwtAuthenticationFilter.class);

        // Special handling for H2 console in development
        http.headers(headers -> 
            headers.frameOptions(frameOptions -> frameOptions.sameOrigin()) // Allow H2 console frames
        );

        return http.build();
    }
}