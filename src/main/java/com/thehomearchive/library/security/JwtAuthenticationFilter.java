package com.thehomearchive.library.security;

import com.thehomearchive.library.config.JwtConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * 
 * Processes JWT tokens from incoming requests and sets up Spring Security authentication context.
 * This filter runs once per request and validates JWT tokens in the Authorization header.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                  @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        final String authorizationHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwtToken = null;

        // Extract JWT token from Authorization header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
            try {
                username = jwtConfig.extractUsername(jwtToken);
            } catch (Exception e) {
                logger.debug("Failed to extract username from JWT: {}", e.getMessage());
            }
        }

        // If we have a username and no existing authentication in SecurityContext
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            try {
                // Load user details
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                // Validate token
                if (jwtConfig.validateToken(jwtToken, userDetails.getUsername())) {
                    
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );
                    
                    // Set authentication details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    logger.debug("Successfully authenticated user: {}", username);
                } else {
                    logger.debug("JWT token validation failed for user: {}", username);
                }
                
            } catch (Exception e) {
                logger.warn("Failed to authenticate user {}: {}", username, e.getMessage());
            }
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Determine if this filter should not be applied to certain requests
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Skip JWT processing for public endpoints
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/public/") ||
               path.startsWith("/h2-console/") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/actuator/info") ||
               path.startsWith("/api-docs/") ||
               path.startsWith("/swagger-ui/") ||
               path.equals("/swagger-ui.html") ||
               path.equals("/favicon.ico") ||
               path.equals("/error");
    }
}