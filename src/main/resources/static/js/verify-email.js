/**
 * Email Verification Page JavaScript
 * Handles email verification process and token validation
 */

document.addEventListener('DOMContentLoaded', function() {
    // Get URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    const fromRegistration = urlParams.get('fromRegistration') === 'true';

    // Get state elements
    const verificationProcessing = document.getElementById('verification-processing');
    const verificationSuccess = document.getElementById('verification-success');
    const verificationError = document.getElementById('verification-error');
    const manualVerification = document.getElementById('manual-verification');
    const errorDetails = document.getElementById('error-details');

    // Get form elements for manual verification
    const manualVerifyForm = document.getElementById('manual-verify-form');
    const verificationTokenInput = document.getElementById('verification-token');
    const manualVerifyBtn = document.getElementById('manual-verify-btn');
    const manualVerifyBtnText = manualVerifyBtn.querySelector('.btn-text');
    const manualVerifyBtnLoading = manualVerifyBtn.querySelector('.btn-loading');

    // Get resend buttons
    const resendVerificationBtn = document.getElementById('resend-verification');
    const resendFromManualBtn = document.getElementById('resend-from-manual');

    /**
     * Show specific verification state
     */
    function showState(stateName) {
        const states = ['processing', 'success', 'error', 'manual'];
        
        states.forEach(state => {
            const element = document.getElementById(`verification-${state}`);
            if (element) {
                element.style.display = state === stateName ? 'block' : 'none';
            }
        });
    }

    /**
     * Set loading state for manual verification
     */
    function setManualLoadingState(loading) {
        manualVerifyBtn.disabled = loading;
        
        if (loading) {
            manualVerifyBtnText.style.display = 'none';
            manualVerifyBtnLoading.style.display = 'inline';
        } else {
            manualVerifyBtnText.style.display = 'inline';
            manualVerifyBtnLoading.style.display = 'none';
        }
    }

    /**
     * Set loading state for resend buttons
     */
    function setResendLoadingState(button, loading) {
        if (!button) return;
        
        button.disabled = loading;
        
        const btnText = button.querySelector('.btn-text');
        const btnLoading = button.querySelector('.btn-loading');
        
        if (btnText && btnLoading) {
            if (loading) {
                btnText.style.display = 'none';
                btnLoading.style.display = 'inline';
            } else {
                btnText.style.display = 'inline';
                btnLoading.style.display = 'none';
            }
        } else {
            // Simple button without loading indicator
            button.textContent = loading ? 'Sending...' : 'Send New Verification Email';
        }
    }

    /**
     * Verify email with token
     */
    async function verifyEmailToken(verificationToken) {
        try {
            const result = await authService.verifyEmail(verificationToken);
            
            if (result.success) {
                showState('success');
                
                // Update user data if provided
                if (result.user) {
                    authService.setUser(result.user);
                }
                
                // Announce success to screen readers
                announceToScreenReader('Email verification successful');
                
            } else {
                showState('error');
                errorDetails.textContent = result.message || 'Email verification failed. Please try again.';
                
                // Announce error to screen readers
                announceToScreenReader(`Email verification failed: ${result.message}`);
            }
        } catch (error) {
            console.error('Email verification error:', error);
            showState('error');
            errorDetails.textContent = 'An unexpected error occurred during verification. Please try again.';
            
            // Announce error to screen readers
            announceToScreenReader('An unexpected error occurred during verification');
        }
    }

    /**
     * Handle manual verification form submission
     */
    async function handleManualVerification(event) {
        event.preventDefault();
        
        const token = verificationTokenInput.value.trim();
        
        if (!token) {
            showFieldError('token', ['Verification code is required']);
            return;
        }

        // Clear any previous errors
        showFieldError('token', []);
        
        setManualLoadingState(true);
        
        try {
            await verifyEmailToken(token);
        } finally {
            setManualLoadingState(false);
        }
    }

    /**
     * Resend verification email
     */
    async function resendVerificationEmail(button) {
        // Get email from stored user data or prompt user
        let email = null;
        const currentUser = authService.getCurrentUser();
        
        if (currentUser && currentUser.email) {
            email = currentUser.email;
        } else {
            // Prompt user for email if not available
            email = prompt('Please enter your email address to resend verification:');
            if (!email || !email.trim()) {
                return;
            }
            email = email.trim();
        }

        setResendLoadingState(button, true);

        try {
            const result = await authService.resendEmailVerification(email);
            
            if (result.success) {
                // Show success message
                const successMessage = result.message || 'Verification email sent successfully. Please check your inbox.';
                
                // Create temporary success notification
                const notification = document.createElement('div');
                notification.className = 'message success-message';
                notification.textContent = successMessage;
                notification.style.marginTop = '1rem';
                
                button.parentNode.insertBefore(notification, button.nextSibling);
                
                // Remove notification after 5 seconds
                setTimeout(() => {
                    if (notification.parentNode) {
                        notification.parentNode.removeChild(notification);
                    }
                }, 5000);
                
                // Announce success to screen readers
                announceToScreenReader(successMessage);
                
            } else {
                // Show error message
                const errorMessage = result.message || 'Failed to resend verification email. Please try again.';
                
                // Create temporary error notification
                const notification = document.createElement('div');
                notification.className = 'message error-message';
                notification.textContent = errorMessage;
                notification.style.marginTop = '1rem';
                
                button.parentNode.insertBefore(notification, button.nextSibling);
                
                // Remove notification after 5 seconds
                setTimeout(() => {
                    if (notification.parentNode) {
                        notification.parentNode.removeChild(notification);
                    }
                }, 5000);
                
                // Announce error to screen readers
                announceToScreenReader(errorMessage);
            }
        } catch (error) {
            console.error('Resend verification error:', error);
            announceToScreenReader('Failed to resend verification email');
        } finally {
            setResendLoadingState(button, false);
        }
    }

    /**
     * Show field error for manual verification
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
     * Announce message to screen readers
     */
    function announceToScreenReader(message) {
        const announcement = document.createElement('div');
        announcement.setAttribute('aria-live', 'polite');
        announcement.setAttribute('aria-atomic', 'true');
        announcement.className = 'sr-only';
        announcement.textContent = message;
        
        document.body.appendChild(announcement);
        
        // Remove after announcement
        setTimeout(() => {
            if (announcement.parentNode) {
                announcement.parentNode.removeChild(announcement);
            }
        }, 1000);
    }

    /**
     * Setup event listeners
     */
    function setupEventListeners() {
        // Manual verification form
        if (manualVerifyForm) {
            manualVerifyForm.addEventListener('submit', handleManualVerification);
        }

        // Resend verification buttons
        if (resendVerificationBtn) {
            resendVerificationBtn.addEventListener('click', () => {
                resendVerificationEmail(resendVerificationBtn);
            });
        }

        if (resendFromManualBtn) {
            resendFromManualBtn.addEventListener('click', () => {
                resendVerificationEmail(resendFromManualBtn);
            });
        }

        // Real-time validation for manual verification
        if (verificationTokenInput) {
            verificationTokenInput.addEventListener('input', function() {
                if (this.value.trim()) {
                    showFieldError('token', []);
                }
            });
        }
    }

    /**
     * Setup accessibility features
     */
    function setupAccessibility() {
        // Focus management based on current state
        const activeState = document.querySelector('.verification-state[style*="block"]');
        if (activeState) {
            const focusableElement = activeState.querySelector('button, input, a');
            if (focusableElement) {
                focusableElement.focus();
            }
        }
    }

    /**
     * Initialize the page based on current state
     */
    function init() {
        // Check if user is already authenticated and verified
        if (authService.isAuthenticated()) {
            const currentUser = authService.getCurrentUser();
            if (currentUser && currentUser.emailVerified) {
                // User is already verified, redirect to dashboard
                window.location.href = '/dashboard.html';
                return;
            }
        }

        if (token) {
            // URL contains verification token, start automatic verification
            showState('processing');
            verifyEmailToken(token);
        } else if (fromRegistration) {
            // User just registered, show manual verification form
            showState('manual');
            
            // Pre-fill with guidance
            announceToScreenReader('Registration successful. Please check your email for a verification link, or enter your verification code below.');
            
        } else {
            // Default state - show manual verification
            showState('manual');
        }

        setupEventListeners();
        setupAccessibility();

        console.log('Email verification page initialized');
    }

    // Initialize when DOM is ready
    init();
});