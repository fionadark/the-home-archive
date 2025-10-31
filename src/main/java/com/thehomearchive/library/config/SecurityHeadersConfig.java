package com.thehomearchive.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Security Headers Configuration for Dark Academia Library Application
 * 
 * Configures comprehensive security headers to protect against various web vulnerabilities:
 * - XSS (Cross-Site Scripting)
 * - Clickjacking
 * - MIME type sniffing
 * - Information disclosure
 * - Content injection
 * 
 * This configuration adds defense-in-depth security measures beyond Spring Security's
 * authentication and authorization mechanisms.
 */
@Configuration
public class SecurityHeadersConfig {

    /**
     * Security headers filter that adds comprehensive security headers to all responses.
     * 
     * These headers provide protection against common web vulnerabilities and are
     * recommended by OWASP and security best practices.
     */
    @Bean
    public OncePerRequestFilter securityHeadersFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, 
                                          HttpServletResponse response, 
                                          FilterChain filterChain) throws ServletException, IOException {
                
                // Content Security Policy - Prevents XSS and data injection attacks
                // Allow scripts and styles from same origin, and specific trusted CDNs
                String csp = "default-src 'self'; " +
                           "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                           "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net; " +
                           "font-src 'self' https://fonts.gstatic.com data:; " +
                           "img-src 'self' data: https: blob:; " +
                           "connect-src 'self' https://openlibrary.org https://covers.openlibrary.org; " +
                           "frame-ancestors 'none'; " +
                           "base-uri 'self'; " +
                           "form-action 'self'";
                response.setHeader("Content-Security-Policy", csp);
                
                // X-Frame-Options - Prevents clickjacking attacks
                response.setHeader("X-Frame-Options", "DENY");
                
                // X-Content-Type-Options - Prevents MIME type sniffing
                response.setHeader("X-Content-Type-Options", "nosniff");
                
                // X-XSS-Protection - Enables XSS filtering (legacy browsers)
                response.setHeader("X-XSS-Protection", "1; mode=block");
                
                // Referrer Policy - Controls referrer information in requests
                response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                
                // Strict Transport Security - Enforces HTTPS connections
                // Only add if request is over HTTPS to avoid browser warnings
                if (request.isSecure()) {
                    response.setHeader("Strict-Transport-Security", 
                        "max-age=31536000; includeSubDomains; preload");
                }
                
                // Permissions Policy - Controls browser features and APIs
                String permissionsPolicy = "camera=(), " +
                                         "microphone=(), " +
                                         "geolocation=(), " +
                                         "payment=(), " +
                                         "accelerometer=(), " +
                                         "gyroscope=(), " +
                                         "magnetometer=(), " +
                                         "usb=(), " +
                                         "bluetooth=()";
                response.setHeader("Permissions-Policy", permissionsPolicy);
                
                // Cross-Origin Embedder Policy - Enables cross-origin isolation
                response.setHeader("Cross-Origin-Embedder-Policy", "credentialless");
                
                // Cross-Origin Opener Policy - Prevents cross-origin window access
                response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
                
                // Cross-Origin Resource Policy - Controls cross-origin resource loading
                response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
                
                // Cache Control for sensitive pages
                String requestURI = request.getRequestURI();
                if (isSensitivePage(requestURI)) {
                    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    response.setHeader("Pragma", "no-cache");
                    response.setHeader("Expires", "0");
                }
                
                // Add security headers for API responses
                if (requestURI.startsWith("/api/")) {
                    response.setHeader("X-Content-Type-Options", "nosniff");
                    response.setHeader("X-Frame-Options", "DENY");
                }
                
                filterChain.doFilter(request, response);
            }
            
            /**
             * Determines if a page contains sensitive information that should not be cached
             */
            private boolean isSensitivePage(String uri) {
                return uri.contains("/dashboard") || 
                       uri.contains("/library") || 
                       uri.contains("/profile") ||
                       uri.startsWith("/api/") ||
                       uri.contains("/admin");
            }
        };
    }
}