package com.thehomearchive.library.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityHeadersConfigTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldApplySecurityHeaders() {
        String url = "http://localhost:" + port + "/actuator/health";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        
        HttpHeaders responseHeaders = response.getHeaders();
        
        // Verify critical security headers are present
        List<String> cspHeaders = responseHeaders.get("Content-Security-Policy");
        assertThat(cspHeaders)
            .as("Content-Security-Policy header should be present")
            .isNotNull()
            .isNotEmpty();
        
        if (cspHeaders != null && !cspHeaders.isEmpty()) {
            String csp = cspHeaders.get(0);
            assertThat(csp)
                .as("CSP should contain frame-ancestors protection")
                .contains("frame-ancestors 'none'")
                .contains("default-src 'self'");
        }
        
        assertThat(responseHeaders.get("X-Frame-Options"))
            .as("X-Frame-Options header should be present")
            .isNotNull()
            .isNotEmpty();
        
        assertThat(responseHeaders.get("X-Content-Type-Options"))
            .as("X-Content-Type-Options header should be present")
            .isNotNull()
            .contains("nosniff");
        
        assertThat(responseHeaders.get("X-XSS-Protection"))
            .as("X-XSS-Protection header should be present")
            .isNotNull()
            .contains("1; mode=block");
        
        assertThat(responseHeaders.get("Permissions-Policy"))
            .as("Permissions-Policy header should be present")
            .isNotNull()
            .isNotEmpty();
        
        assertThat(responseHeaders.get("Cross-Origin-Embedder-Policy"))
            .as("Cross-Origin-Embedder-Policy header should be present")
            .isNotNull()
            .isNotEmpty();
        
        assertThat(responseHeaders.get("Referrer-Policy"))
            .as("Referrer-Policy header should be present")
            .isNotNull()
            .contains("strict-origin-when-cross-origin");
    }
    
    @Test
    void shouldHandleSecurityHeadersOnPublicEndpoints() {
        String url = "http://localhost:" + port + "/api/auth/register";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.OPTIONS, entity, String.class);
        
        HttpHeaders responseHeaders = response.getHeaders();
        
        // Verify headers are still applied to public endpoints
        assertThat(responseHeaders.get("X-Frame-Options"))
            .as("X-Frame-Options should be present on public endpoints")
            .isNotNull()
            .isNotEmpty();
        
        assertThat(responseHeaders.get("X-Content-Type-Options"))
            .as("X-Content-Type-Options should be present on public endpoints")
            .isNotNull()
            .contains("nosniff");
        
        assertThat(responseHeaders.get("Content-Security-Policy"))
            .as("CSP should be present on public endpoints")
            .isNotNull()
            .isNotEmpty();
    }
}