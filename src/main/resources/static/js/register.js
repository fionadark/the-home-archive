/**
 * Registration Page JavaScript
 * Handles user registration form submission and validation
 */

document.addEventListener('DOMContentLoaded', function() {
    // Check if user is already authenticated
    if (authService.isAuthenticated()) {
        window.location.href = '/dashboard.html';
        return;
    }

    // Get form elements
    const registerForm = document.getElementById('register-form');
    const firstNameInput = document.getElementById('first-name');
    const lastNameInput = document.getElementById('last-name');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirm-password');
    const termsInput = document.getElementById('terms-agreement');
    const registerBtn = document.getElementById('register-btn');
    const registerBtnText = registerBtn.querySelector('.btn-text');
    const registerBtnLoading = registerBtn.querySelector('.btn-loading');

    // Validation regex patterns
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/;

    /**
     * Validate individual form fields
     */
    function validateField(field, value, confirmValue = null) {
        const errors = [];

        switch (field) {
            case 'firstName':
                if (!value.trim()) {
                    errors.push('First name is required');
                } else if (value.trim().length < 1) {
                    errors.push('First name must be at least 1 character');
                } else if (value.trim().length > 100) {
                    errors.push('First name must not exceed 100 characters');
                }
                break;

            case 'lastName':
                if (!value.trim()) {
                    errors.push('Last name is required');
                } else if (value.trim().length < 1) {
                    errors.push('Last name must be at least 1 character');
                } else if (value.trim().length > 100) {
                    errors.push('Last name must not exceed 100 characters');
                }
                break;

            case 'email':
                if (!value.trim()) {
                    errors.push('Email is required');
                } else if (!emailRegex.test(value.trim())) {
                    errors.push('Please enter a valid email address');
                } else if (value.trim().length > 255) {
                    errors.push('Email must not exceed 255 characters');
                }
                break;

            case 'password':
                if (!value) {
                    errors.push('Password is required');
                } else if (value.length < 8) {
                    errors.push('Password must be at least 8 characters');
                } else if (!passwordRegex.test(value)) {
                    errors.push('Password must contain uppercase, lowercase, number, and special character');
                }
                break;

            case 'confirmPassword':
                if (!value) {
                    errors.push('Please confirm your password');
                } else if (value !== confirmValue) {
                    errors.push('Passwords do not match');
                }
                break;

            case 'agreeToTerms':
                if (!value) {
                    errors.push('You must agree to the Terms of Service and Privacy Policy');
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
        const firstName = firstNameInput.value.trim();
        const lastName = lastNameInput.value.trim();
        const email = emailInput.value.trim();
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;
        const agreeToTerms = termsInput.checked;

        const firstNameErrors = validateField('firstName', firstName);
        const lastNameErrors = validateField('lastName', lastName);
        const emailErrors = validateField('email', email);
        const passwordErrors = validateField('password', password);
        const confirmPasswordErrors = validateField('confirmPassword', confirmPassword, password);
        const termsErrors = validateField('agreeToTerms', agreeToTerms);

        showFieldError('first-name', firstNameErrors);
        showFieldError('last-name', lastNameErrors);
        showFieldError('email', emailErrors);
        showFieldError('password', passwordErrors);
        showFieldError('confirm-password', confirmPasswordErrors);
        showFieldError('terms', termsErrors);

        return firstNameErrors.length === 0 && 
               lastNameErrors.length === 0 && 
               emailErrors.length === 0 && 
               passwordErrors.length === 0 && 
               confirmPasswordErrors.length === 0 && 
               termsErrors.length === 0;
    }

    /**
     * Set loading state
     */
    function setLoadingState(loading) {
        registerBtn.disabled = loading;
        
        if (loading) {
            registerBtnText.style.display = 'none';
            registerBtnLoading.style.display = 'inline';
        } else {
            registerBtnText.style.display = 'inline';
            registerBtnLoading.style.display = 'none';
        }
    }

    /**
     * Handle successful registration
     */
    function handleRegistrationSuccess(result) {
        authService.showMessage(
            result.message || 'Registration successful! Please check your email for verification.',
            'success'
        );
        
        // Update registration status for screen readers
        const registerStatus = document.getElementById('register-status');
        if (registerStatus) {
            registerStatus.textContent = 'Registration successful, please check your email';
        }

        // Clear form
        registerForm.reset();

        // Redirect to email verification page after delay
        setTimeout(() => {
            window.location.href = '/verify-email.html?fromRegistration=true';
        }, 3000);
    }

    /**
     * Handle registration error
     */
    function handleRegistrationError(result) {
        authService.showMessage(result.message, 'error');
        
        // Update registration status for screen readers
        const registerStatus = document.getElementById('register-status');
        if (registerStatus) {
            registerStatus.textContent = `Registration failed: ${result.message}`;
        }

        // Display field-specific errors if available
        if (result.errors && Array.isArray(result.errors)) {
            authService.displayFieldErrors(result.errors, registerForm);
        }

        // Focus on first field with error
        const firstErrorField = registerForm.querySelector('.field-error.show');
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

        const formData = {
            firstName: firstNameInput.value.trim(),
            lastName: lastNameInput.value.trim(),
            email: emailInput.value.trim(),
            password: passwordInput.value,
            agreeToTerms: termsInput.checked
        };

        setLoadingState(true);

        try {
            const result = await authService.register(formData);

            if (result.success) {
                handleRegistrationSuccess(result);
            } else {
                handleRegistrationError(result);
            }
        } catch (error) {
            console.error('Registration error:', error);
            authService.showMessage('An unexpected error occurred. Please try again.', 'error');
        } finally {
            setLoadingState(false);
        }
    }

    /**
     * Real-time field validation
     */
    function setupFieldValidation() {
        // First name validation
        firstNameInput.addEventListener('blur', function() {
            const errors = validateField('firstName', this.value.trim());
            showFieldError('first-name', errors);
        });

        // Last name validation
        lastNameInput.addEventListener('blur', function() {
            const errors = validateField('lastName', this.value.trim());
            showFieldError('last-name', errors);
        });

        // Email validation
        emailInput.addEventListener('blur', function() {
            const errors = validateField('email', this.value.trim());
            showFieldError('email', errors);
        });

        // Password validation
        passwordInput.addEventListener('blur', function() {
            const errors = validateField('password', this.value);
            showFieldError('password', errors);
        });

        // Confirm password validation
        confirmPasswordInput.addEventListener('blur', function() {
            const errors = validateField('confirmPassword', this.value, passwordInput.value);
            showFieldError('confirm-password', errors);
        });

        // Re-validate confirm password when password changes
        passwordInput.addEventListener('input', function() {
            if (confirmPasswordInput.value) {
                const errors = validateField('confirmPassword', confirmPasswordInput.value, this.value);
                showFieldError('confirm-password', errors);
            }
        });

        // Terms validation
        termsInput.addEventListener('change', function() {
            const errors = validateField('agreeToTerms', this.checked);
            showFieldError('terms', errors);
        });

        // Clear errors on input
        [firstNameInput, lastNameInput, emailInput, passwordInput, confirmPasswordInput].forEach(input => {
            input.addEventListener('input', function() {
                if (this.value.trim()) {
                    const fieldName = this.name === 'firstName' ? 'first-name' :
                                    this.name === 'lastName' ? 'last-name' :
                                    this.name === 'confirmPassword' ? 'confirm-password' :
                                    this.name;
                    showFieldError(fieldName, []);
                }
            });
        });
    }

    /**
     * Password strength indicator
     */
    function setupPasswordStrengthIndicator() {
        const passwordHelp = document.getElementById('password-help');
        
        passwordInput.addEventListener('input', function() {
            const password = this.value;
            const requirements = [
                { test: password.length >= 8, text: 'At least 8 characters' },
                { test: /[a-z]/.test(password), text: 'Lowercase letter' },
                { test: /[A-Z]/.test(password), text: 'Uppercase letter' },
                { test: /\d/.test(password), text: 'Number' },
                { test: /[@$!%*?&]/.test(password), text: 'Special character' }
            ];

            const metRequirements = requirements.filter(req => req.test).length;
            let strengthText = 'Minimum 8 characters with uppercase, lowercase, number, and special character';
            let strengthClass = '';

            if (password.length > 0) {
                if (metRequirements <= 2) {
                    strengthText = `Weak password (${metRequirements}/5 requirements met)`;
                    strengthClass = 'weak';
                } else if (metRequirements <= 3) {
                    strengthText = `Fair password (${metRequirements}/5 requirements met)`;
                    strengthClass = 'fair';
                } else if (metRequirements <= 4) {
                    strengthText = `Good password (${metRequirements}/5 requirements met)`;
                    strengthClass = 'good';
                } else {
                    strengthText = `Strong password (${metRequirements}/5 requirements met)`;
                    strengthClass = 'strong';
                }
            }

            passwordHelp.textContent = strengthText;
            passwordHelp.className = `field-help ${strengthClass}`;
        });
    }

    /**
     * Setup accessibility features
     */
    function setupAccessibility() {
        // Enhance keyboard navigation
        registerForm.addEventListener('keydown', function(event) {
            if (event.key === 'Enter' && !registerBtn.disabled) {
                // Don't submit if user is in a text input (let them add newlines if needed)
                if (event.target.tagName === 'INPUT' && event.target.type === 'text') {
                    return;
                }
                handleSubmit(event);
            }
        });

        // Add ARIA descriptions for complex fields
        const emailInput = document.getElementById('email');
        emailInput.setAttribute('aria-describedby', 'email-help email-error');
        
        const passwordInput = document.getElementById('password');
        passwordInput.setAttribute('aria-describedby', 'password-help password-error');
    }

    /**
     * Initialize the page
     */
    function init() {
        setupFieldValidation();
        setupPasswordStrengthIndicator();
        setupAccessibility();

        // Attach form submission handler
        registerForm.addEventListener('submit', handleSubmit);

        // Focus on first name field
        firstNameInput.focus();

        console.log('Registration page initialized');
    }

    // Initialize when DOM is ready
    init();
});