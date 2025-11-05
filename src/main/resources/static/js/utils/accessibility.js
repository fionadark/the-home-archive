/**
 * Accessibility Utilities for The Home Archive - Dark Academia Library
 * Provides ARIA labels, keyboard navigation, and accessibility enhancements
 * Ensures compliance with WCAG 2.1 AA standards
 */

(function() {
    'use strict';

    /**
     * AccessibilityManager - Central accessibility management
     */
    class AccessibilityManager {
        constructor() {
            this.focusableElements = [
                'button', 'input', 'select', 'textarea', 'a[href]', 
                '[tabindex]:not([tabindex="-1"])', '[contenteditable]'
            ];
            this.trapStack = [];
            this.announcementRegion = null;
            this.init();
        }

        /**
         * Initialize accessibility features
         */
        init() {
            this.createAnnouncementRegion();
            this.setupGlobalKeyboardHandlers();
            this.enhanceExistingElements();
            this.setupSkipLinks();
        }

        /**
         * Create live announcement region for screen readers
         */
        createAnnouncementRegion() {
            if (this.announcementRegion) return;

            this.announcementRegion = document.createElement('div');
            this.announcementRegion.setAttribute('aria-live', 'polite');
            this.announcementRegion.setAttribute('aria-atomic', 'true');
            this.announcementRegion.setAttribute('aria-label', 'Status updates');
            this.announcementRegion.style.cssText = `
                position: absolute;
                left: -10000px;
                width: 1px;
                height: 1px;
                overflow: hidden;
            `;
            document.body.appendChild(this.announcementRegion);
        }

        /**
         * Announce message to screen readers
         * @param {string} message - Message to announce
         * @param {string} priority - 'polite' or 'assertive'
         */
        announce(message, priority = 'polite') {
            if (!this.announcementRegion) {
                this.createAnnouncementRegion();
            }

            this.announcementRegion.setAttribute('aria-live', priority);
            this.announcementRegion.textContent = message;

            // Clear after announcement
            setTimeout(() => {
                this.announcementRegion.textContent = '';
            }, 1000);
        }

        /**
         * Setup global keyboard navigation handlers
         */
        setupGlobalKeyboardHandlers() {
            document.addEventListener('keydown', (event) => {
                this.handleGlobalKeydown(event);
            });

            // Handle focus visible for keyboard users
            document.addEventListener('keydown', (event) => {
                if (event.key === 'Tab') {
                    document.body.classList.add('keyboard-navigation');
                }
            });

            document.addEventListener('mousedown', () => {
                document.body.classList.remove('keyboard-navigation');
            });
        }

        /**
         * Handle global keyboard events
         * @param {KeyboardEvent} event - Keyboard event
         */
        handleGlobalKeydown(event) {
            const { key, altKey, ctrlKey, metaKey } = event;

            // Skip link navigation (Alt + S)
            if (altKey && key.toLowerCase() === 's') {
                event.preventDefault();
                this.activateSkipLinks();
                return;
            }

            // Quick navigation shortcuts
            if (altKey && !ctrlKey && !metaKey) {
                switch (key.toLowerCase()) {
                    case 'h':
                        event.preventDefault();
                        this.focusElement('h1, h2, h3, h4, h5, h6');
                        this.announce('Navigated to main heading');
                        break;
                    case 'm':
                        event.preventDefault();
                        this.focusElement('main, [role="main"]');
                        this.announce('Navigated to main content');
                        break;
                    case 'n':
                        event.preventDefault();
                        this.focusElement('nav, [role="navigation"]');
                        this.announce('Navigated to navigation');
                        break;
                    case 'f':
                        event.preventDefault();
                        this.focusElement('form, [role="form"]');
                        this.announce('Navigated to form');
                        break;
                    case 'b':
                        event.preventDefault();
                        this.focusElement('button, [role="button"]');
                        this.announce('Navigated to button');
                        break;
                }
            }

            // Escape key handling
            if (key === 'Escape') {
                this.handleEscape();
            }
        }

        /**
         * Focus on element matching selector
         * @param {string} selector - CSS selector
         */
        focusElement(selector) {
            const element = document.querySelector(selector);
            if (element) {
                element.focus();
                if (element.scrollIntoView) {
                    element.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        }

        /**
         * Handle escape key
         */
        handleEscape() {
            // Close modals
            const modal = document.querySelector('.modal.active, .modal-overlay.active');
            if (modal) {
                this.closeModal(modal);
                return;
            }

            // Close dropdowns
            const dropdown = document.querySelector('.dropdown.open');
            if (dropdown) {
                this.closeDropdown(dropdown);
                return;
            }

            // Exit focus trap
            if (this.trapStack.length > 0) {
                this.exitFocusTrap();
            }
        }

        /**
         * Setup skip links for better navigation
         */
        setupSkipLinks() {
            if (document.querySelector('.skip-links')) return;

            const skipLinks = document.createElement('div');
            skipLinks.className = 'skip-links';
            skipLinks.innerHTML = `
                <a href="#main-content" class="skip-link">Skip to main content</a>
                <a href="#navigation" class="skip-link">Skip to navigation</a>
                <a href="#search" class="skip-link">Skip to search</a>
            `;

            // Add styles
            const style = document.createElement('style');
            style.textContent = `
                .skip-links {
                    position: absolute;
                    top: 0;
                    left: 0;
                    z-index: 9999;
                }
                .skip-link {
                    position: absolute;
                    top: -40px;
                    left: 6px;
                    background: var(--da-gold, #d4af37);
                    color: var(--da-dark, #1a1a1a);
                    padding: 8px 16px;
                    text-decoration: none;
                    border-radius: 0 0 4px 4px;
                    font-weight: bold;
                    font-size: 14px;
                    transition: top 0.3s ease;
                }
                .skip-link:focus {
                    top: 0;
                }
            `;
            document.head.appendChild(style);
            document.body.insertBefore(skipLinks, document.body.firstChild);
        }

        /**
         * Activate skip links
         */
        activateSkipLinks() {
            const skipLink = document.querySelector('.skip-link');
            if (skipLink) {
                skipLink.focus();
            }
        }

        /**
         * Enhance existing elements with accessibility features
         */
        enhanceExistingElements() {
            this.enhanceForms();
            this.enhanceButtons();
            this.enhanceLinks();
            this.enhanceImages();
            this.enhanceInteractiveElements();
            this.enhanceRegions();
        }

        /**
         * Enhance forms with accessibility
         */
        enhanceForms() {
            document.querySelectorAll('form').forEach(form => {
                if (!form.getAttribute('role')) {
                    form.setAttribute('role', 'form');
                }

                // Enhance form inputs
                form.querySelectorAll('input, select, textarea').forEach(input => {
                    this.enhanceInput(input);
                });

                // Add form validation announcements
                form.addEventListener('submit', (event) => {
                    if (!form.checkValidity()) {
                        event.preventDefault();
                        this.announceFormErrors(form);
                    }
                });
            });
        }

        /**
         * Enhance individual input element
         * @param {HTMLElement} input - Input element
         */
        enhanceInput(input) {
            const label = this.findLabelForInput(input);
            
            // Ensure input has accessible name
            if (!input.getAttribute('aria-label') && !input.getAttribute('aria-labelledby') && !label) {
                const placeholder = input.getAttribute('placeholder');
                if (placeholder) {
                    input.setAttribute('aria-label', placeholder);
                }
            }

            // Add required field indication
            if (input.hasAttribute('required')) {
                input.setAttribute('aria-required', 'true');
                
                // Add visual indicator
                if (label && !label.querySelector('.required-indicator')) {
                    const indicator = document.createElement('span');
                    indicator.className = 'required-indicator';
                    indicator.textContent = ' *';
                    indicator.setAttribute('aria-label', 'required');
                    label.appendChild(indicator);
                }
            }

            // Add validation feedback
            input.addEventListener('invalid', () => {
                this.handleInputInvalid(input);
            });

            input.addEventListener('input', () => {
                this.clearInputError(input);
            });
        }

        /**
         * Find label for input element
         * @param {HTMLElement} input - Input element
         * @returns {HTMLElement|null} Label element
         */
        findLabelForInput(input) {
            if (input.id) {
                return document.querySelector(`label[for="${input.id}"]`);
            }
            return input.closest('label');
        }

        /**
         * Handle input validation error
         * @param {HTMLElement} input - Input element
         */
        handleInputInvalid(input) {
            const errorId = `${input.id || 'input'}-error`;
            let errorElement = document.getElementById(errorId);

            if (!errorElement) {
                errorElement = document.createElement('div');
                errorElement.id = errorId;
                errorElement.className = 'error-message';
                errorElement.setAttribute('role', 'alert');
                input.parentNode.insertBefore(errorElement, input.nextSibling);
            }

            errorElement.textContent = input.validationMessage;
            input.setAttribute('aria-describedby', errorId);
            input.setAttribute('aria-invalid', 'true');
        }

        /**
         * Clear input error
         * @param {HTMLElement} input - Input element
         */
        clearInputError(input) {
            const errorId = `${input.id || 'input'}-error`;
            const errorElement = document.getElementById(errorId);

            if (errorElement) {
                errorElement.textContent = '';
            }

            input.removeAttribute('aria-describedby');
            input.removeAttribute('aria-invalid');
        }

        /**
         * Announce form errors
         * @param {HTMLElement} form - Form element
         */
        announceFormErrors(form) {
            const invalidInputs = form.querySelectorAll(':invalid');
            const errorCount = invalidInputs.length;

            if (errorCount > 0) {
                this.announce(`Form has ${errorCount} error${errorCount > 1 ? 's' : ''}. Please correct and try again.`, 'assertive');
                
                // Focus first invalid input
                invalidInputs[0].focus();
            }
        }

        /**
         * Enhance buttons with accessibility
         */
        enhanceButtons() {
            document.querySelectorAll('button, [role="button"]').forEach(button => {
                // Ensure buttons have accessible names
                if (!this.hasAccessibleName(button)) {
                    const icon = button.querySelector('i, .icon');
                    if (icon) {
                        const ariaLabel = this.inferButtonPurpose(button);
                        if (ariaLabel) {
                            button.setAttribute('aria-label', ariaLabel);
                        }
                    }
                }

                // Add keyboard support for non-button elements
                if (button.tagName !== 'BUTTON') {
                    button.setAttribute('tabindex', '0');
                    button.addEventListener('keydown', (event) => {
                        if (event.key === 'Enter' || event.key === ' ') {
                            event.preventDefault();
                            button.click();
                        }
                    });
                }

                // Add loading state support
                this.addLoadingStateSupport(button);
            });
        }

        /**
         * Add loading state support to button
         * @param {HTMLElement} button - Button element
         */
        addLoadingStateSupport(button) {
            const originalMethod = button.click;
            button.click = function(...args) {
                if (this.hasAttribute('aria-busy')) return;
                return originalMethod.apply(this, args);
            };
        }

        /**
         * Check if element has accessible name
         * @param {HTMLElement} element - Element to check
         * @returns {boolean} Has accessible name
         */
        hasAccessibleName(element) {
            return !!(
                element.getAttribute('aria-label') ||
                element.getAttribute('aria-labelledby') ||
                element.textContent.trim() ||
                element.getAttribute('title')
            );
        }

        /**
         * Infer button purpose from context
         * @param {HTMLElement} button - Button element
         * @returns {string|null} Inferred purpose
         */
        inferButtonPurpose(button) {
            const classes = button.className.toLowerCase();
            const icon = button.querySelector('i, .icon');
            
            if (classes.includes('close')) return 'Close';
            if (classes.includes('delete')) return 'Delete';
            if (classes.includes('edit')) return 'Edit';
            if (classes.includes('add')) return 'Add';
            if (classes.includes('save')) return 'Save';
            if (classes.includes('cancel')) return 'Cancel';
            if (classes.includes('submit')) return 'Submit';
            if (classes.includes('search')) return 'Search';
            if (classes.includes('menu')) return 'Menu';
            
            if (icon) {
                const iconClasses = icon.className.toLowerCase();
                if (iconClasses.includes('close') || iconClasses.includes('times')) return 'Close';
                if (iconClasses.includes('delete') || iconClasses.includes('trash')) return 'Delete';
                if (iconClasses.includes('edit') || iconClasses.includes('pencil')) return 'Edit';
                if (iconClasses.includes('add') || iconClasses.includes('plus')) return 'Add';
                if (iconClasses.includes('save') || iconClasses.includes('check')) return 'Save';
                if (iconClasses.includes('search')) return 'Search';
                if (iconClasses.includes('menu') || iconClasses.includes('bars')) return 'Menu';
            }

            return null;
        }

        /**
         * Enhance links with accessibility
         */
        enhanceLinks() {
            document.querySelectorAll('a').forEach(link => {
                // External links
                if (link.hostname && link.hostname !== window.location.hostname) {
                    link.setAttribute('rel', 'noopener noreferrer');
                    
                    if (!link.textContent.includes('(opens in new window)')) {
                        link.setAttribute('aria-label', `${link.textContent.trim()} (opens in new window)`);
                    }
                }

                // Download links
                if (link.hasAttribute('download')) {
                    if (!link.textContent.includes('download')) {
                        link.setAttribute('aria-label', `Download ${link.textContent.trim()}`);
                    }
                }

                // Links without meaningful text
                if (!this.hasAccessibleName(link)) {
                    const href = link.getAttribute('href');
                    if (href && href !== '#') {
                        link.setAttribute('aria-label', `Link to ${href}`);
                    }
                }
            });
        }

        /**
         * Enhance images with accessibility
         */
        enhanceImages() {
            document.querySelectorAll('img').forEach(img => {
                // Decorative images
                if (!img.getAttribute('alt') && this.isDecorativeImage(img)) {
                    img.setAttribute('alt', '');
                    img.setAttribute('role', 'presentation');
                }

                // Missing alt text
                if (!img.hasAttribute('alt')) {
                    const title = img.getAttribute('title') || img.getAttribute('data-title');
                    if (title) {
                        img.setAttribute('alt', title);
                    } else {
                        console.warn('Image missing alt text:', img.src);
                        img.setAttribute('alt', 'Image');
                    }
                }
            });
        }

        /**
         * Check if image is decorative
         * @param {HTMLImageElement} img - Image element
         * @returns {boolean} Is decorative
         */
        isDecorativeImage(img) {
            const classes = img.className.toLowerCase();
            return classes.includes('decorative') || 
                   classes.includes('background') || 
                   classes.includes('icon');
        }

        /**
         * Enhance interactive elements
         */
        enhanceInteractiveElements() {
            // Dropdown toggles
            document.querySelectorAll('[data-toggle="dropdown"]').forEach(toggle => {
                toggle.setAttribute('aria-haspopup', 'true');
                toggle.setAttribute('aria-expanded', 'false');
                
                toggle.addEventListener('click', () => {
                    const expanded = toggle.getAttribute('aria-expanded') === 'true';
                    toggle.setAttribute('aria-expanded', (!expanded).toString());
                });
            });

            // Modal triggers
            document.querySelectorAll('[data-toggle="modal"]').forEach(trigger => {
                trigger.addEventListener('click', (event) => {
                    const modalId = trigger.getAttribute('data-target');
                    if (modalId) {
                        this.openModal(modalId, trigger);
                    }
                });
            });

            // Tab panels
            this.enhanceTabPanels();

            // Accordions
            this.enhanceAccordions();
        }

        /**
         * Enhance tab panels
         */
        enhanceTabPanels() {
            document.querySelectorAll('[role="tablist"]').forEach(tablist => {
                const tabs = tablist.querySelectorAll('[role="tab"]');
                
                tabs.forEach((tab, index) => {
                    tab.setAttribute('tabindex', index === 0 ? '0' : '-1');
                    
                    tab.addEventListener('keydown', (event) => {
                        this.handleTabKeydown(event, tabs, index);
                    });
                });
            });
        }

        /**
         * Handle tab keydown navigation
         * @param {KeyboardEvent} event - Keyboard event
         * @param {NodeList} tabs - Tab elements
         * @param {number} currentIndex - Current tab index
         */
        handleTabKeydown(event, tabs, currentIndex) {
            const { key } = event;
            let newIndex = currentIndex;

            switch (key) {
                case 'ArrowRight':
                case 'ArrowDown':
                    event.preventDefault();
                    newIndex = (currentIndex + 1) % tabs.length;
                    break;
                case 'ArrowLeft':
                case 'ArrowUp':
                    event.preventDefault();
                    newIndex = currentIndex === 0 ? tabs.length - 1 : currentIndex - 1;
                    break;
                case 'Home':
                    event.preventDefault();
                    newIndex = 0;
                    break;
                case 'End':
                    event.preventDefault();
                    newIndex = tabs.length - 1;
                    break;
                default:
                    return;
            }

            // Update tab focus and selection
            tabs[currentIndex].setAttribute('tabindex', '-1');
            tabs[newIndex].setAttribute('tabindex', '0');
            tabs[newIndex].focus();
            
            // Activate tab if configured
            if (tabs[newIndex].click) {
                tabs[newIndex].click();
            }
        }

        /**
         * Enhance accordions
         */
        enhanceAccordions() {
            document.querySelectorAll('.accordion').forEach(accordion => {
                const buttons = accordion.querySelectorAll('.accordion-button, [data-toggle="accordion"]');
                
                buttons.forEach(button => {
                    button.setAttribute('aria-expanded', 'false');
                    
                    const target = button.getAttribute('data-target') || 
                                 button.getAttribute('href')?.substring(1);
                    
                    if (target) {
                        const panel = document.getElementById(target);
                        if (panel) {
                            panel.setAttribute('role', 'region');
                            panel.setAttribute('aria-labelledby', button.id || `accordion-${target}`);
                            
                            if (!button.id) {
                                button.id = `accordion-${target}`;
                            }
                        }
                    }
                });
            });
        }

        /**
         * Enhance page regions
         */
        enhanceRegions() {
            // Main content
            if (!document.querySelector('main, [role="main"]')) {
                const main = document.querySelector('.main-content, .content, #content');
                if (main && !main.getAttribute('role')) {
                    main.setAttribute('role', 'main');
                }
            }

            // Navigation
            document.querySelectorAll('nav').forEach(nav => {
                if (!nav.getAttribute('aria-label') && !nav.getAttribute('aria-labelledby')) {
                    nav.setAttribute('aria-label', 'Navigation menu');
                }
            });

            // Search regions
            document.querySelectorAll('.search, [role="search"]').forEach(search => {
                search.setAttribute('role', 'search');
                
                if (!search.getAttribute('aria-label')) {
                    search.setAttribute('aria-label', 'Search');
                }
            });

            // Complementary content
            document.querySelectorAll('.sidebar, .aside').forEach(aside => {
                if (!aside.getAttribute('role')) {
                    aside.setAttribute('role', 'complementary');
                }
            });
        }

        /**
         * Focus trap management for modals and dropdowns
         */

        /**
         * Create focus trap
         * @param {HTMLElement} container - Container element
         * @param {HTMLElement} returnFocus - Element to return focus to
         */
        createFocusTrap(container, returnFocus = null) {
            const focusableElements = container.querySelectorAll(this.focusableElements.join(', '));
            
            if (focusableElements.length === 0) return;

            const firstElement = focusableElements[0];
            const lastElement = focusableElements[focusableElements.length - 1];

            const trap = {
                container,
                firstElement,
                lastElement,
                returnFocus: returnFocus || document.activeElement,
                handleKeydown: (event) => {
                    if (event.key === 'Tab') {
                        if (event.shiftKey) {
                            if (document.activeElement === firstElement) {
                                event.preventDefault();
                                lastElement.focus();
                            }
                        } else {
                            if (document.activeElement === lastElement) {
                                event.preventDefault();
                                firstElement.focus();
                            }
                        }
                    }
                }
            };

            container.addEventListener('keydown', trap.handleKeydown);
            this.trapStack.push(trap);

            // Focus first element
            firstElement.focus();

            return trap;
        }

        /**
         * Exit focus trap
         */
        exitFocusTrap() {
            const trap = this.trapStack.pop();
            if (trap) {
                trap.container.removeEventListener('keydown', trap.handleKeydown);
                if (trap.returnFocus) {
                    trap.returnFocus.focus();
                }
            }
        }

        /**
         * Modal accessibility management
         */

        /**
         * Open modal with accessibility support
         * @param {string} modalId - Modal ID
         * @param {HTMLElement} trigger - Trigger element
         */
        openModal(modalId, trigger) {
            const modal = document.getElementById(modalId) || document.querySelector(modalId);
            if (!modal) return;

            // Set up modal attributes
            modal.setAttribute('role', 'dialog');
            modal.setAttribute('aria-modal', 'true');
            
            const title = modal.querySelector('.modal-title, h1, h2, h3');
            if (title && !modal.getAttribute('aria-labelledby')) {
                if (!title.id) title.id = `${modalId}-title`;
                modal.setAttribute('aria-labelledby', title.id);
            }

            // Hide background content from screen readers
            const body = document.body;
            const siblings = Array.from(body.children).filter(child => child !== modal);
            siblings.forEach(sibling => {
                if (!sibling.getAttribute('aria-hidden')) {
                    sibling.setAttribute('aria-hidden', 'true');
                    sibling.setAttribute('data-modal-hidden', 'true');
                }
            });

            // Create focus trap
            this.createFocusTrap(modal, trigger);

            // Announce modal opening
            this.announce('Dialog opened', 'assertive');
        }

        /**
         * Close modal
         * @param {HTMLElement} modal - Modal element
         */
        closeModal(modal) {
            // Restore background content
            document.querySelectorAll('[data-modal-hidden]').forEach(element => {
                element.removeAttribute('aria-hidden');
                element.removeAttribute('data-modal-hidden');
            });

            // Exit focus trap
            this.exitFocusTrap();

            // Announce modal closing
            this.announce('Dialog closed', 'assertive');
        }

        /**
         * Close dropdown
         * @param {HTMLElement} dropdown - Dropdown element
         */
        closeDropdown(dropdown) {
            dropdown.classList.remove('open');
            
            const trigger = document.querySelector(`[aria-controls="${dropdown.id}"]`);
            if (trigger) {
                trigger.setAttribute('aria-expanded', 'false');
                trigger.focus();
            }
        }

        /**
         * Set button loading state
         * @param {HTMLElement} button - Button element
         * @param {boolean} loading - Loading state
         * @param {string} loadingText - Loading text
         */
        setButtonLoading(button, loading, loadingText = 'Loading...') {
            if (loading) {
                button.setAttribute('aria-busy', 'true');
                button.setAttribute('disabled', 'true');
                
                if (!button.getAttribute('data-original-text')) {
                    button.setAttribute('data-original-text', button.textContent);
                }
                
                button.textContent = loadingText;
                this.announce(`${button.getAttribute('data-original-text')} is loading`, 'polite');
            } else {
                button.removeAttribute('aria-busy');
                button.removeAttribute('disabled');
                
                const originalText = button.getAttribute('data-original-text');
                if (originalText) {
                    button.textContent = originalText;
                    button.removeAttribute('data-original-text');
                }
            }
        }

        /**
         * Update page title and announce navigation
         * @param {string} title - New page title
         * @param {boolean} announce - Whether to announce the change
         */
        updatePageTitle(title, announce = true) {
            document.title = title;
            
            if (announce) {
                this.announce(`Navigated to ${title}`, 'polite');
            }
        }

        /**
         * Enhance search functionality
         * @param {HTMLElement} searchInput - Search input element
         * @param {HTMLElement} resultsContainer - Results container
         */
        enhanceSearch(searchInput, resultsContainer) {
            if (!searchInput || !resultsContainer) return;

            // Set up search attributes
            searchInput.setAttribute('role', 'combobox');
            searchInput.setAttribute('aria-expanded', 'false');
            searchInput.setAttribute('aria-autocomplete', 'list');
            
            // Set up results container
            resultsContainer.setAttribute('role', 'listbox');
            if (!resultsContainer.id) {
                resultsContainer.id = 'search-results';
            }
            searchInput.setAttribute('aria-owns', resultsContainer.id);

            // Handle search result updates
            const observer = new MutationObserver(() => {
                const results = resultsContainer.querySelectorAll('[role="option"], .search-result');
                searchInput.setAttribute('aria-expanded', results.length > 0 ? 'true' : 'false');
                
                if (results.length > 0) {
                    this.announce(`${results.length} search results found`, 'polite');
                    
                    // Enhance result navigation
                    results.forEach((result, index) => {
                        result.setAttribute('role', 'option');
                        result.setAttribute('tabindex', '-1');
                        
                        result.addEventListener('keydown', (event) => {
                            this.handleSearchResultKeydown(event, results, index, searchInput);
                        });
                    });
                }
            });

            observer.observe(resultsContainer, { childList: true, subtree: true });

            // Handle search input navigation
            searchInput.addEventListener('keydown', (event) => {
                const results = resultsContainer.querySelectorAll('[role="option"], .search-result');
                
                if (event.key === 'ArrowDown' && results.length > 0) {
                    event.preventDefault();
                    results[0].focus();
                } else if (event.key === 'Escape') {
                    searchInput.setAttribute('aria-expanded', 'false');
                    resultsContainer.innerHTML = '';
                }
            });
        }

        /**
         * Handle search result keyboard navigation
         * @param {KeyboardEvent} event - Keyboard event
         * @param {NodeList} results - Result elements
         * @param {number} currentIndex - Current result index
         * @param {HTMLElement} searchInput - Search input element
         */
        handleSearchResultKeydown(event, results, currentIndex, searchInput) {
            const { key } = event;
            
            switch (key) {
                case 'ArrowDown':
                    event.preventDefault();
                    const nextIndex = (currentIndex + 1) % results.length;
                    results[nextIndex].focus();
                    break;
                case 'ArrowUp':
                    event.preventDefault();
                    if (currentIndex === 0) {
                        searchInput.focus();
                    } else {
                        results[currentIndex - 1].focus();
                    }
                    break;
                case 'Enter':
                    event.preventDefault();
                    results[currentIndex].click();
                    break;
                case 'Escape':
                    event.preventDefault();
                    searchInput.focus();
                    searchInput.setAttribute('aria-expanded', 'false');
                    break;
            }
        }
    }

    /**
     * Accessibility utilities for dynamic content
     */
    const AccessibilityUtils = {
        /**
         * Make table accessible
         * @param {HTMLElement} table - Table element
         */
        enhanceTable(table) {
            if (!table) return;

            // Add table caption if missing
            if (!table.querySelector('caption') && table.getAttribute('data-title')) {
                const caption = document.createElement('caption');
                caption.textContent = table.getAttribute('data-title');
                table.insertBefore(caption, table.firstChild);
            }

            // Enhance headers
            const headers = table.querySelectorAll('th');
            headers.forEach(header => {
                if (!header.getAttribute('scope')) {
                    const isColumnHeader = header.closest('thead') || 
                                         header.parentElement.children[0] === header;
                    header.setAttribute('scope', isColumnHeader ? 'col' : 'row');
                }
            });

            // Add sortable attributes
            table.querySelectorAll('th[data-sortable]').forEach(header => {
                header.setAttribute('role', 'columnheader');
                header.setAttribute('tabindex', '0');
                header.setAttribute('aria-sort', 'none');
                
                header.addEventListener('keydown', (event) => {
                    if (event.key === 'Enter' || event.key === ' ') {
                        event.preventDefault();
                        header.click();
                    }
                });
            });
        },

        /**
         * Make card grid accessible
         * @param {HTMLElement} grid - Grid container
         */
        enhanceCardGrid(grid) {
            if (!grid) return;

            grid.setAttribute('role', 'grid');
            
            const cards = grid.querySelectorAll('.card, .book-card, [data-card]');
            cards.forEach((card, index) => {
                card.setAttribute('role', 'gridcell');
                card.setAttribute('tabindex', index === 0 ? '0' : '-1');
                
                card.addEventListener('keydown', (event) => {
                    this.handleGridNavigation(event, cards, index);
                });
            });
        },

        /**
         * Handle grid keyboard navigation
         * @param {KeyboardEvent} event - Keyboard event
         * @param {NodeList} cards - Card elements
         * @param {number} currentIndex - Current card index
         */
        handleGridNavigation(event, cards, currentIndex) {
            const { key } = event;
            const gridWidth = Math.floor(Math.sqrt(cards.length)); // Approximate grid width
            let newIndex = currentIndex;

            switch (key) {
                case 'ArrowRight':
                    event.preventDefault();
                    newIndex = Math.min(currentIndex + 1, cards.length - 1);
                    break;
                case 'ArrowLeft':
                    event.preventDefault();
                    newIndex = Math.max(currentIndex - 1, 0);
                    break;
                case 'ArrowDown':
                    event.preventDefault();
                    newIndex = Math.min(currentIndex + gridWidth, cards.length - 1);
                    break;
                case 'ArrowUp':
                    event.preventDefault();
                    newIndex = Math.max(currentIndex - gridWidth, 0);
                    break;
                case 'Home':
                    event.preventDefault();
                    newIndex = 0;
                    break;
                case 'End':
                    event.preventDefault();
                    newIndex = cards.length - 1;
                    break;
                default:
                    return;
            }

            if (newIndex !== currentIndex) {
                cards[currentIndex].setAttribute('tabindex', '-1');
                cards[newIndex].setAttribute('tabindex', '0');
                cards[newIndex].focus();
            }
        },

        /**
         * Announce dynamic content changes
         * @param {string} message - Announcement message
         * @param {string} priority - Priority level
         */
        announce(message, priority = 'polite') {
            if (window.accessibility) {
                window.accessibility.announce(message, priority);
            }
        }
    };

    // Initialize accessibility manager
    const accessibility = new AccessibilityManager();

    // Add CSS for keyboard navigation and accessibility
    const accessibilityStyles = document.createElement('style');
    accessibilityStyles.textContent = `
        /* Keyboard navigation focus styles */
        .keyboard-navigation *:focus {
            outline: 2px solid var(--da-gold, #d4af37);
            outline-offset: 2px;
        }

        /* High contrast mode support */
        @media (prefers-contrast: high) {
            :root {
                --da-dark: #000000;
                --da-light: #ffffff;
                --da-gold: #ffff00;
            }
        }

        /* Reduced motion support */
        @media (prefers-reduced-motion: reduce) {
            *, *::before, *::after {
                animation-duration: 0.01ms !important;
                animation-iteration-count: 1 !important;
                transition-duration: 0.01ms !important;
            }
        }

        /* Error message styling */
        .error-message {
            color: #dc3545;
            font-size: 0.875em;
            margin-top: 0.25rem;
        }

        /* Required field indicator */
        .required-indicator {
            color: #dc3545;
            font-weight: bold;
        }

        /* Focus trap styling */
        .focus-trap-active {
            outline: none;
        }

        /* Screen reader only content */
        .sr-only {
            position: absolute;
            width: 1px;
            height: 1px;
            padding: 0;
            margin: -1px;
            overflow: hidden;
            clip: rect(0, 0, 0, 0);
            white-space: nowrap;
            border: 0;
        }

        /* Improved button states */
        button[aria-busy="true"] {
            opacity: 0.7;
            cursor: not-allowed;
        }

        /* Table accessibility */
        th[aria-sort="ascending"]::after {
            content: " ↑";
        }

        th[aria-sort="descending"]::after {
            content: " ↓";
        }

        /* Grid cell focus */
        [role="gridcell"]:focus {
            z-index: 1;
            position: relative;
        }
    `;
    document.head.appendChild(accessibilityStyles);

    // Export to global scope
    window.accessibility = accessibility;
    window.AccessibilityUtils = AccessibilityUtils;

    // Auto-enhance on DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            accessibility.enhanceExistingElements();
        });
    } else {
        accessibility.enhanceExistingElements();
    }

})();