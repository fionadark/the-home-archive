package com.thehomearchive.library.config;

import com.thehomearchive.library.service.AuthenticationService;
import com.thehomearchive.library.service.EmailService;
import com.thehomearchive.library.service.UserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

/**
 * Test configuration to provide mock beans for testing.
 * This helps resolve circular dependencies in tests.
 */
@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public UserService userService() {
        return mock(UserService.class);
    }
    
    @Bean
    @Primary
    public AuthenticationService authenticationService() {
        return mock(AuthenticationService.class);
    }
    
    @Bean
    @Primary
    public EmailService emailService() {
        return mock(EmailService.class);
    }
}