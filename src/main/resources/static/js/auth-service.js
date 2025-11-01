/**
 * Authentication Service for The Home Archive
 * Handles JWT token management and API communication with backend
 * Compliant with Spring Boot 3.x REST API and CORS configuration
 */

class AuthService {
    constructor() {
        this.baseURL = ''; // Spring Boot serves from same origin
        this.apiBaseURL = '/api/auth';
        this.tokenKey = 'homeArchive_accessToken';
        this.refreshTokenKey = 'homeArchive_refreshToken';
        this.userKey = 'homeArchive_user';
        
        // Initialize CSRF token if available
        this.csrfToken = this.getCSRFToken();
        
        // Set up axios-like request interceptor functionality
        this.setupTokenRefresh();
    }

    /**
     * Get CSRF token from meta tag (Spring Security requirement)
     */
    getCSRFToken() {
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
        
        if (csrfMeta && csrfHeaderMeta) {
            return {
                token: csrfMeta.getAttribute('content'),
                header: csrfHeaderMeta.getAttribute('content')
            };
        }
        return null;
    }

    /**
     * Make authenticated API request with proper headers
     */
    async makeRequest(url, options = {}) {
        const token = this.getAccessToken();
        
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...options.headers
        };

        // Add CSRF token if available
        if (this.csrfToken) {
            headers[this.csrfToken.header] = this.csrfToken.token;
        }

        // Add JWT token if available
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            method: options.method || 'GET',
            headers,
            credentials: 'same-origin', // Important for CORS
            ...options
        };

        if (config.method !== 'GET' && options.body) {
            config.body = typeof options.body === 'string' ? options.body : JSON.stringify(options.body);
        }

        try {
            const response = await fetch(url, config);
            
            // Handle token refresh if 401 Unauthorized
            if (response.status === 401 && token) {
                const refreshed = await this.refreshAccessToken();
                if (refreshed) {
                    // Retry original request with new token
                    headers['Authorization'] = `Bearer ${this.getAccessToken()}`;
                    config.headers = headers;
                    return await fetch(url, config);
                } else {
                    // Refresh failed, redirect to login
                    this.logout();
                    window.location.href = '/login.html';
                    throw new Error('Session expired. Please login again.');
                }
            }

            return response;
        } catch (error) {
            console.error('Request failed:', error);
            throw error;
        }
    }

    /**
     * Register a new user
     */
    async register(userData) {
        try {
            const response = await this.makeRequest(`${this.apiBaseURL}/register`, {
                method: 'POST',
                body: userData
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Registration failed');
            }

            return {
                success: true,
                message: data.message || 'Registration successful. Please check your email for verification.',
                user: data.user
            };
        } catch (error) {
            return {
                success: false,
                message: error.message || 'Registration failed. Please try again.',
                errors: error.errors || []
            };
        }
    }

    /**
     * Login user with email and password
     */
    async login(email, password, rememberMe = false) {
        try {
            const response = await this.makeRequest(`${this.apiBaseURL}/login`, {
                method: 'POST',
                body: {
                    email,
                    password,
                    rememberMe
                }
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Login failed');
            }

            // Store tokens and user data
            this.setAccessToken(data.accessToken);
            if (data.refreshToken) {
                this.setRefreshToken(data.refreshToken);
            }
            
            if (data.user) {
                this.setUser(data.user);
            }

            return {
                success: true,
                message: 'Login successful',
                user: data.user,
                requiresEmailVerification: data.requiresEmailVerification || false
            };
        } catch (error) {
            return {
                success: false,
                message: error.message || 'Login failed. Please check your credentials.',
                errors: error.errors || []
            };
        }
    }

    /**
     * Logout user and clear tokens
     */
    async logout() {
        try {
            // Notify backend of logout
            await this.makeRequest(`${this.apiBaseURL}/logout`, {
                method: 'POST'
            });
        } catch (error) {
            console.warn('Logout request failed:', error);
            // Continue with local logout even if backend call fails
        }

        // Clear all stored data
        this.clearTokens();
        this.clearUser();
        
        return {
            success: true,
            message: 'Logged out successfully'
        };
    }

    /**
     * Verify email with token
     */
    async verifyEmail(token) {
        try {
            const response = await this.makeRequest(`${this.apiBaseURL}/verify-email`, {
                method: 'POST',
                body: { token }
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Email verification failed');
            }

            return {
                success: true,
                message: data.message || 'Email verified successfully',
                user: data.user
            };
        } catch (error) {
            return {
                success: false,
                message: error.message || 'Email verification failed. Please try again.',
                errors: error.errors || []
            };
        }
    }

    /**
     * Resend email verification
     */
    async resendEmailVerification(email) {
        try {
            const response = await this.makeRequest(`${this.apiBaseURL}/resend-verification`, {
                method: 'POST',
                body: { email }
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to resend verification email');
            }

            return {
                success: true,
                message: data.message || 'Verification email sent successfully'
            };
        } catch (error) {
            return {
                success: false,
                message: error.message || 'Failed to resend verification email. Please try again.',
                errors: error.errors || []
            };
        }
    }

    /**
     * Refresh access token using refresh token
     */
    async refreshAccessToken() {
        const refreshToken = this.getRefreshToken();
        if (!refreshToken) {
            return false;
        }

        try {
            const response = await fetch(`${this.apiBaseURL}/refresh-token`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                credentials: 'same-origin',
                body: JSON.stringify({ refreshToken })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Token refresh failed');
            }

            // Update stored tokens
            this.setAccessToken(data.accessToken);
            if (data.refreshToken) {
                this.setRefreshToken(data.refreshToken);
            }

            return true;
        } catch (error) {
            console.error('Token refresh failed:', error);
            this.clearTokens();
            return false;
        }
    }

    /**
     * Get current user profile
     */
    async getUserProfile() {
        try {
            const response = await this.makeRequest(`${this.apiBaseURL}/profile`);

            if (!response.ok) {
                throw new Error('Failed to fetch user profile');
            }

            const user = await response.json();
            this.setUser(user);
            
            return {
                success: true,
                user
            };
        } catch (error) {
            return {
                success: false,
                message: error.message || 'Failed to fetch user profile',
                errors: error.errors || []
            };
        }
    }

    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        const token = this.getAccessToken();
        if (!token) {
            return false;
        }

        try {
            const payload = this.decodeJWT(token);
            const currentTime = Math.floor(Date.now() / 1000);
            return payload.exp > currentTime;
        } catch (error) {
            console.error('Invalid token:', error);
            this.clearTokens();
            return false;
        }
    }

    /**
     * Get current user from storage
     */
    getCurrentUser() {
        try {
            const userData = localStorage.getItem(this.userKey);
            return userData ? JSON.parse(userData) : null;
        } catch (error) {
            console.error('Error parsing user data:', error);
            return null;
        }
    }

    /**
     * Setup automatic token refresh
     */
    setupTokenRefresh() {
        // Check token expiration every 5 minutes
        setInterval(() => {
            const token = this.getAccessToken();
            if (token) {
                try {
                    const payload = this.decodeJWT(token);
                    const currentTime = Math.floor(Date.now() / 1000);
                    const timeUntilExpiry = payload.exp - currentTime;
                    
                    // Refresh if token expires in less than 5 minutes
                    if (timeUntilExpiry < 300) {
                        this.refreshAccessToken().catch(error => {
                            console.error('Automatic token refresh failed:', error);
                        });
                    }
                } catch (error) {
                    console.error('Error checking token expiration:', error);
                }
            }
        }, 5 * 60 * 1000); // 5 minutes
    }

    /**
     * Decode JWT token payload
     */
    decodeJWT(token) {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));

            return JSON.parse(jsonPayload);
        } catch (error) {
            throw new Error('Invalid token format');
        }
    }

    /**
     * Token management methods
     */
    getAccessToken() {
        return localStorage.getItem(this.tokenKey);
    }

    setAccessToken(token) {
        localStorage.setItem(this.tokenKey, token);
    }

    getRefreshToken() {
        return localStorage.getItem(this.refreshTokenKey);
    }

    setRefreshToken(token) {
        localStorage.setItem(this.refreshTokenKey, token);
    }

    clearTokens() {
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem(this.refreshTokenKey);
    }

    setUser(user) {
        localStorage.setItem(this.userKey, JSON.stringify(user));
    }

    clearUser() {
        localStorage.removeItem(this.userKey);
    }

    /**
     * Utility method to handle form validation errors
     */
    displayFieldErrors(errors, formElement) {
        // Clear previous errors
        const errorElements = formElement.querySelectorAll('.field-error');
        errorElements.forEach(el => {
            el.textContent = '';
            el.classList.remove('show');
        });

        // Display new errors
        if (Array.isArray(errors)) {
            errors.forEach(error => {
                if (error.field) {
                    const errorElement = formElement.querySelector(`#${error.field}-error`);
                    if (errorElement) {
                        errorElement.textContent = error.message;
                        errorElement.classList.add('show');
                    }
                }
            });
        }
    }

    /**
     * Show success or error message
     */
    showMessage(message, type = 'success', containerId = 'message-container') {
        const container = document.getElementById(containerId);
        if (!container) return;

        const messageElement = container.querySelector(`.${type}-message`);
        if (!messageElement) return;

        messageElement.textContent = message;
        messageElement.style.display = 'block';
        container.style.display = 'block';

        // Hide other message types
        const otherType = type === 'success' ? 'error' : 'success';
        const otherElement = container.querySelector(`.${otherType}-message`);
        if (otherElement) {
            otherElement.style.display = 'none';
        }

        // Auto-hide success messages after 5 seconds
        if (type === 'success') {
            setTimeout(() => {
                messageElement.style.display = 'none';
                container.style.display = 'none';
            }, 5000);
        }
    }

    /**
     * Hide all messages
     */
    hideMessages(containerId = 'message-container') {
        const container = document.getElementById(containerId);
        if (container) {
            container.style.display = 'none';
            const messages = container.querySelectorAll('.message');
            messages.forEach(msg => msg.style.display = 'none');
        }
    }
}

// Create global instance
const authService = new AuthService();

// Export for module use if needed
if (typeof module !== 'undefined' && module.exports) {
    module.exports = AuthService;
}