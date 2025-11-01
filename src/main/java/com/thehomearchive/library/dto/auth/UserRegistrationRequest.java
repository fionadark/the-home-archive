package com.thehomearchive.library.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user registration requests.
 * Contains validation rules for user registration data.
 */
public class UserRegistrationRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character"
    )
    private String password;
    
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;
    
    // Default constructor for Jackson
    public UserRegistrationRequest() {}
    
    // All-args constructor
    public UserRegistrationRequest(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    // Getters
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    // Setters
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    /**
     * Create a builder for UserRegistrationRequest.
     *
     * @return UserRegistrationRequestBuilder instance
     */
    public static UserRegistrationRequestBuilder builder() {
        return new UserRegistrationRequestBuilder();
    }
    
    /**
     * Builder class for UserRegistrationRequest.
     */
    public static class UserRegistrationRequestBuilder {
        
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        
        public UserRegistrationRequestBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserRegistrationRequestBuilder password(String password) {
            this.password = password;
            return this;
        }
        
        public UserRegistrationRequestBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public UserRegistrationRequestBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public UserRegistrationRequest build() {
            return new UserRegistrationRequest(email, password, firstName, lastName);
        }
    }
    
    @Override
    public String toString() {
        return "UserRegistrationRequest{" +
                "email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}'; // Exclude password for security
    }
}