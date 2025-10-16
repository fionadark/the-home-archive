package com.homearchive.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * CORS and Security Headers configuration for the Home Archive application.
 * Provides production-ready security headers and cross-origin request policies.
 */
@Configuration
public class CorsConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);
    
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String allowedOrigins;
    
    @Value("${app.security.headers.enabled:true}")
    private boolean securityHeadersEnabled;
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.info("Configuring CORS with allowed origins: {}", allowedOrigins);
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Parse allowed origins from configuration
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOriginPatterns(origins);
        
        // Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));
        
        // Allow specific headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Origin",
            "Content-Type",
            "Accept",
            "Authorization",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-Requested-With",
            "X-API-Key",
            "Cache-Control",
            "Pragma"
        ));
        
        // Expose specific headers to the client
        configuration.setExposedHeaders(Arrays.asList(
            "Content-Type",
            "Cache-Control",
            "Content-Language",
            "Content-Length",
            "Last-Modified"
        ));
        
        // Allow credentials
        configuration.setAllowCredentials(true);
        
        // Cache preflight responses for 1 hour
        configuration.setMaxAge(3600L);
        
        // Apply CORS configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    /**
     * Security headers filter that adds production-ready security headers to all responses.
     * Implements OWASP recommended security headers for web application protection.
     */
    @Bean
    @Order(1)
    public Filter securityHeadersFilter() {
        return new SecurityHeadersFilter();
    }
    
    /**
     * Custom filter that adds comprehensive security headers to HTTP responses.
     */
    public class SecurityHeadersFilter implements Filter {
        
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            logger.info("Security headers filter initialized (enabled: {})", securityHeadersEnabled);
        }
        
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            
            if (securityHeadersEnabled && response instanceof HttpServletResponse) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                
                // Add security headers
                addSecurityHeaders(httpResponse, httpRequest);
            }
            
            chain.doFilter(request, response);
        }
        
        /**
         * Add comprehensive security headers to HTTP response.
         */
        private void addSecurityHeaders(HttpServletResponse response, HttpServletRequest request) {
            // Content Security Policy - restrict resource loading
            response.setHeader("Content-Security-Policy", 
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: blob:; " +
                "font-src 'self'; " +
                "connect-src 'self'; " +
                "media-src 'self'; " +
                "object-src 'none'; " +
                "child-src 'self'; " +
                "frame-ancestors 'none'; " +
                "form-action 'self'; " +
                "base-uri 'self'");
            
            // X-Content-Type-Options - prevent MIME type sniffing
            response.setHeader("X-Content-Type-Options", "nosniff");
            
            // X-Frame-Options - prevent clickjacking
            response.setHeader("X-Frame-Options", "DENY");
            
            // X-XSS-Protection - enable XSS filtering
            response.setHeader("X-XSS-Protection", "1; mode=block");
            
            // Referrer-Policy - control referrer information
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            
            // Permissions-Policy - control browser features
            response.setHeader("Permissions-Policy", 
                "camera=(), microphone=(), geolocation=(), payment=(), usb=()");
            
            // Strict-Transport-Security - enforce HTTPS (only for HTTPS requests)
            if (request.isSecure() || "https".equals(request.getHeader("X-Forwarded-Proto"))) {
                response.setHeader("Strict-Transport-Security", 
                    "max-age=31536000; includeSubDomains; preload");
            }
            
            // Cache-Control for API responses
            String path = request.getRequestURI();
            if (path.startsWith("/api/")) {
                if (path.contains("/search") || path.contains("/books")) {
                    // Cache search results for 5 minutes
                    response.setHeader("Cache-Control", "public, max-age=300");
                } else {
                    // Don't cache dynamic API responses
                    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    response.setHeader("Pragma", "no-cache");
                    response.setHeader("Expires", "0");
                }
            }
            
            // Server header - hide server information
            response.setHeader("Server", "HomeArchive");
            
            // X-Powered-By - remove default server headers
            response.setHeader("X-Powered-By", "");
            
            logger.debug("Security headers added to response for: {}", request.getRequestURI());
        }
        
        @Override
        public void destroy() {
            logger.info("Security headers filter destroyed");
        }
    }
}