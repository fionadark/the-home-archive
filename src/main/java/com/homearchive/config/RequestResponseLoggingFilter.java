package com.homearchive.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

/**
 * Request/Response logging filter for debugging and monitoring.
 * Logs detailed information about HTTP requests and responses for troubleshooting.
 */
@Component
@Order(2) // Run after security headers filter
public class RequestResponseLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final Logger accessLogger = LoggerFactory.getLogger("ACCESS");
    
    @Value("${app.logging.request-response.enabled:true}")
    private boolean loggingEnabled;
    
    @Value("${app.logging.request-response.include-payload:false}")
    private boolean includePayload;
    
    @Value("${app.logging.request-response.max-payload-length:1000}")
    private int maxPayloadLength;
    
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_MDC_KEY = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (!loggingEnabled || !(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Generate or extract request ID for correlation
        String requestId = getOrGenerateRequestId(httpRequest);
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        
        // Add request ID to response headers
        httpResponse.setHeader(REQUEST_ID_HEADER, requestId);
        
        try {
            Instant startTime = Instant.now();
            
            // Wrap request and response for content capturing if needed
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);
            
            // Log incoming request
            logRequest(wrappedRequest, requestId);
            
            // Continue with the filter chain
            chain.doFilter(wrappedRequest, wrappedResponse);
            
            // Calculate processing time
            Duration processingTime = Duration.between(startTime, Instant.now());
            
            // Log outgoing response
            logResponse(wrappedRequest, wrappedResponse, requestId, processingTime);
            
            // Log access entry for monitoring
            logAccess(wrappedRequest, wrappedResponse, requestId, processingTime);
            
            // Important: Copy cached content to actual response
            wrappedResponse.copyBodyToResponse();
            
        } finally {
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    /**
     * Get existing request ID from header or generate a new one.
     */
    private String getOrGenerateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = UUID.randomUUID().toString().substring(0, 8); // Short UUID for readability
        }
        return requestId;
    }

    /**
     * Log incoming HTTP request details.
     */
    private void logRequest(ContentCachingRequestWrapper request, String requestId) {
        try {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("Incoming Request [").append(requestId).append("] ");
            logMessage.append(request.getMethod()).append(" ");
            logMessage.append(request.getRequestURI());
            
            // Add query string if present
            if (request.getQueryString() != null) {
                logMessage.append("?").append(request.getQueryString());
            }
            
            // Add important headers
            logMessage.append(" | Headers: {");
            Collections.list(request.getHeaderNames()).forEach(headerName -> {
                if (isImportantHeader(headerName)) {
                    logMessage.append(headerName).append("=")
                              .append(request.getHeader(headerName)).append(", ");
                }
            });
            logMessage.append("}");
            
            // Add remote address
            logMessage.append(" | RemoteAddr: ").append(getClientIpAddress(request));
            
            // Add content length
            if (request.getContentLength() > 0) {
                logMessage.append(" | ContentLength: ").append(request.getContentLength());
            }
            
            logger.info(logMessage.toString());
            
            // Log request payload if enabled and present
            if (includePayload && request.getContentLength() > 0) {
                logRequestPayload(request, requestId);
            }
            
        } catch (Exception e) {
            logger.warn("Failed to log request details for [{}]: {}", requestId, e.getMessage());
        }
    }

    /**
     * Log outgoing HTTP response details.
     */
    private void logResponse(ContentCachingRequestWrapper request, 
                           ContentCachingResponseWrapper response, 
                           String requestId, 
                           Duration processingTime) {
        try {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("Outgoing Response [").append(requestId).append("] ");
            logMessage.append("Status: ").append(response.getStatus());
            logMessage.append(" | ProcessingTime: ").append(processingTime.toMillis()).append("ms");
            
            // Add important response headers
            logMessage.append(" | Headers: {");
            response.getHeaderNames().forEach(headerName -> {
                if (isImportantResponseHeader(headerName)) {
                    logMessage.append(headerName).append("=")
                              .append(response.getHeader(headerName)).append(", ");
                }
            });
            logMessage.append("}");
            
            // Add content size
            if (response.getContentSize() > 0) {
                logMessage.append(" | ResponseSize: ").append(response.getContentSize()).append(" bytes");
            }
            
            // Log at appropriate level based on status code
            if (response.getStatus() >= 500) {
                logger.error(logMessage.toString());
            } else if (response.getStatus() >= 400) {
                logger.warn(logMessage.toString());
            } else {
                logger.info(logMessage.toString());
            }
            
            // Log response payload if enabled and present
            if (includePayload && response.getContentSize() > 0) {
                logResponsePayload(response, requestId);
            }
            
        } catch (Exception e) {
            logger.warn("Failed to log response details for [{}]: {}", requestId, e.getMessage());
        }
    }

    /**
     * Log access entry for monitoring and analytics.
     */
    private void logAccess(ContentCachingRequestWrapper request, 
                          ContentCachingResponseWrapper response, 
                          String requestId, 
                          Duration processingTime) {
        try {
            // Common Log Format (CLF) style access log
            String accessLog = String.format("%s - - [%s] \"%s %s %s\" %d %d \"%s\" \"%s\" %dms [%s]",
                getClientIpAddress(request),
                java.time.LocalDateTime.now().toString(),
                request.getMethod(),
                request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""),
                request.getProtocol(),
                response.getStatus(),
                response.getContentSize(),
                request.getHeader("Referer") != null ? request.getHeader("Referer") : "-",
                request.getHeader("User-Agent") != null ? request.getHeader("User-Agent") : "-",
                processingTime.toMillis(),
                requestId
            );
            
            accessLogger.info(accessLog);
            
        } catch (Exception e) {
            logger.warn("Failed to log access entry for [{}]: {}", requestId, e.getMessage());
        }
    }

    /**
     * Log request payload content.
     */
    private void logRequestPayload(ContentCachingRequestWrapper request, String requestId) {
        try {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                String payload = new String(content, StandardCharsets.UTF_8);
                if (payload.length() > maxPayloadLength) {
                    payload = payload.substring(0, maxPayloadLength) + "...";
                }
                logger.debug("Request Payload [{}]: {}", requestId, payload);
            }
        } catch (Exception e) {
            logger.warn("Failed to log request payload for [{}]: {}", requestId, e.getMessage());
        }
    }

    /**
     * Log response payload content.
     */
    private void logResponsePayload(ContentCachingResponseWrapper response, String requestId) {
        try {
            byte[] content = response.getContentAsByteArray();
            if (content.length > 0) {
                String payload = new String(content, StandardCharsets.UTF_8);
                if (payload.length() > maxPayloadLength) {
                    payload = payload.substring(0, maxPayloadLength) + "...";
                }
                logger.debug("Response Payload [{}]: {}", requestId, payload);
            }
        } catch (Exception e) {
            logger.warn("Failed to log response payload for [{}]: {}", requestId, e.getMessage());
        }
    }

    /**
     * Check if header should be logged.
     */
    private boolean isImportantHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.equals("content-type") ||
               lowerName.equals("authorization") ||
               lowerName.equals("accept") ||
               lowerName.equals("user-agent") ||
               lowerName.equals("x-forwarded-for") ||
               lowerName.equals("x-real-ip") ||
               lowerName.startsWith("x-request-");
    }

    /**
     * Check if response header should be logged.
     */
    private boolean isImportantResponseHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.equals("content-type") ||
               lowerName.equals("cache-control") ||
               lowerName.equals("x-request-id") ||
               lowerName.startsWith("x-");
    }

    /**
     * Get client IP address, considering proxy headers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}