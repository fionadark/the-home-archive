/**
 * Login Page JavaScript
 * Handles login form submission and authentication
 */

document.addEventListener('DOMContentLoaded', function() {
    // Check if user is already authenticated
    if (authService.isAuthenticated()) {
        window.location.href = '/dashboard.html';
        return;
    }

    // Get form elements
    const loginForm = document.getElementById('login-form');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const rememberMeInput = document.getElementById('remember-me');
    const loginBtn = document.getElementById('login-btn');
    const loginBtnText = loginBtn.querySelector('.btn-text');
    const loginBtnLoading = loginBtn.querySelector('.btn-loading');

    // Email validation regex
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    /**
     * Validate individual form fields
     */
    function validateField(field, value) {
        const errors = [];

        switch (field) {
            case 'email':
                if (!value.trim()) {
                    errors.push('Email is required');
                } else if (!emailRegex.test(value)) {
                    errors.push('Please enter a valid email address');
                }
                break;

            case 'password':
                if (!value.trim()) {
                    errors.push('Password is required');
                } else if (value.length < 8) {
                    errors.push('Password must be at least 8 characters');
                }
                break;
        }

        return errors;
    }

    /**
     * Display field-specific errors
     */
    function showFieldError(fieldName, errors) {
        const errorElement = document.getElementById(`${fieldName}-error`);
        if (errorElement) {
            if (errors.length > 0) {
                errorElement.textContent = errors[0];
                errorElement.classList.add('show');
            } else {
                errorElement.textContent = '';
                errorElement.classList.remove('show');
            }
        }
    }

    /**
     * Validate entire form
     */
    function validateForm() {
        const email = emailInput.value.trim();
        const password = passwordInput.value;

        const emailErrors = validateField('email', email);
        const passwordErrors = validateField('password', password);

        showFieldError('email', emailErrors);
        showFieldError('password', passwordErrors);

        return emailErrors.length === 0 && passwordErrors.length === 0;
    }

    /**
     * Set loading state
     */
    function setLoadingState(loading) {
        loginBtn.disabled = loading;
        
        if (loading) {
            loginBtnText.style.display = 'none';
            loginBtnLoading.style.display = 'inline';
        } else {
            loginBtnText.style.display = 'inline';
            loginBtnLoading.style.display = 'none';
        }
    }

    /**
     * Handle successful login
     */
    function handleLoginSuccess(result) {
        authService.showMessage('Login successful! Redirecting...', 'success');
        
        // Update login status for screen readers
        const loginStatus = document.getElementById('login-status');
        if (loginStatus) {
            loginStatus.textContent = 'Login successful, redirecting to dashboard';
        }

        // Redirect after short delay to show success message
        setTimeout(() => {
            if (result.requiresEmailVerification) {
                window.location.href = '/verify-email.html';
            } else {
                window.location.href = '/dashboard.html';
            }
        }, 1500);
    }

    /**
     * Handle login error
     */
    function handleLoginError(result) {
        authService.showMessage(result.message, 'error');
        
        // Update login status for screen readers
        const loginStatus = document.getElementById('login-status');
        if (loginStatus) {
            loginStatus.textContent = `Login failed: ${result.message}`;
        }

        // Display field-specific errors if available
        if (result.errors && Array.isArray(result.errors)) {
            authService.displayFieldErrors(result.errors, loginForm);
        }

        // Focus on first field with error
        const firstErrorField = loginForm.querySelector('.field-error.show');
        if (firstErrorField) {
            const fieldId = firstErrorField.id.replace('-error', '');
            const field = document.getElementById(fieldId);
            if (field) {
                field.focus();
            }
        }
    }

    /**
     * Handle form submission
     */
    async function handleSubmit(event) {
        event.preventDefault();
        
        // Hide any previous messages
        authService.hideMessages();

        // Validate form
        if (!validateForm()) {
            return;
        }

        const email = emailInput.value.trim();
        const password = passwordInput.value;
        const rememberMe = rememberMeInput.checked;

        setLoadingState(true);

        try {
            const result = await authService.login(email, password, rememberMe);

            if (result.success) {
                handleLoginSuccess(result);
            } else {
                handleLoginError(result);
            }
        } catch (error) {
            console.error('Login error:', error);
            authService.showMessage('An unexpected error occurred. Please try again.', 'error');
        } finally {
            setLoadingState(false);
        }
    }

    /**
     * Real-time field validation
     */
    function setupFieldValidation() {
        emailInput.addEventListener('blur', function() {
            const errors = validateField('email', this.value.trim());
            showFieldError('email', errors);
        });

        passwordInput.addEventListener('blur', function() {
            const errors = validateField('password', this.value);
            showFieldError('password', errors);
        });

        // Clear errors on input
        emailInput.addEventListener('input', function() {
            if (this.value.trim()) {
                showFieldError('email', []);
            }
        });

        passwordInput.addEventListener('input', function() {
            if (this.value) {
                showFieldError('password', []);
            }
        });
    }

    /**
     * Setup accessibility features
     */
    function setupAccessibility() {
        // Announce form errors to screen readers
        const messageContainer = document.getElementById('message-container');
        if (messageContainer) {
            // Already has role="alert" and aria-live="polite"
        }

        // Enhance keyboard navigation
        loginForm.addEventListener('keydown', function(event) {
            if (event.key === 'Enter' && !loginBtn.disabled) {
                handleSubmit(event);
            }
        });
    }

    /**
     * Check for email verification completion
     */
    function checkEmailVerificationStatus() {
        const urlParams = new URLSearchParams(window.location.search);
        const verified = urlParams.get('verified');
        const message = urlParams.get('message');

        if (verified === 'true') {
            authService.showMessage(
                message || 'Email verified successfully! You can now log in.',
                'success'
            );
        } else if (verified === 'false') {
            authService.showMessage(
                message || 'Email verification failed. Please try again.',
                'error'
            );
        }
    }

    /**
     * Initialize the page
     */
    function init() {
        setupFieldValidation();
        setupAccessibility();
        checkEmailVerificationStatus();

        // Attach form submission handler
        loginForm.addEventListener('submit', handleSubmit);

        // Focus on email field
        emailInput.focus();

        console.log('Login page initialized');
    }

    // Initialize when DOM is ready
    init();
});