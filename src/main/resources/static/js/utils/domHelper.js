/**
 * DOM Helper Utilities
 * Common DOM manipulation functions to reduce code duplication
 */

export class DOMHelper {
    /**
     * Create an element with class names and optional content
     * @param {string} tagName - HTML tag name
     * @param {string|Array} classNames - Class name(s) to add
     * @param {string} textContent - Optional text content
     * @param {Object} attributes - Optional attributes object
     * @returns {HTMLElement} Created element
     */
    static createElement(tagName, classNames = [], textContent = '', attributes = {}) {
        const element = document.createElement(tagName);
        
        // Handle class names
        if (typeof classNames === 'string') {
            element.className = classNames;
        } else if (Array.isArray(classNames)) {
            element.classList.add(...classNames);
        }
        
        // Set text content
        if (textContent) {
            element.textContent = textContent;
        }
        
        // Set attributes
        Object.entries(attributes).forEach(([key, value]) => {
            element.setAttribute(key, value);
        });
        
        return element;
    }

    /**
     * Create an option element for select dropdowns
     * @param {string} value - Option value
     * @param {string} text - Option display text
     * @param {boolean} selected - Whether option is selected
     * @returns {HTMLOptionElement} Created option element
     */
    static createOption(value, text, selected = false) {
        const option = document.createElement('option');
        option.value = value;
        option.textContent = text;
        option.selected = selected;
        return option;
    }

    /**
     * Clear all children from an element
     * @param {HTMLElement} element - Element to clear
     */
    static clearChildren(element) {
        element.innerHTML = '';
    }

    /**
     * Add event listener with optional once flag
     * @param {HTMLElement} element - Element to add listener to
     * @param {string} event - Event type
     * @param {Function} handler - Event handler
     * @param {boolean} once - Whether to trigger only once
     */
    static addEvent(element, event, handler, once = false) {
        element.addEventListener(event, handler, { once });
    }

    /**
     * Create and append child element
     * @param {HTMLElement} parent - Parent element
     * @param {string} tagName - Child tag name
     * @param {string|Array} classNames - Child class names
     * @param {string} textContent - Child text content
     * @param {Object} attributes - Child attributes
     * @returns {HTMLElement} Created child element
     */
    static appendChild(parent, tagName, classNames = [], textContent = '', attributes = {}) {
        const child = this.createElement(tagName, classNames, textContent, attributes);
        parent.appendChild(child);
        return child;
    }

    /**
     * Safe innerHTML setting with sanitization placeholder
     * @param {HTMLElement} element - Element to set content
     * @param {string} html - HTML content
     */
    static setHTML(element, html) {
        // Note: In production, consider using DOMPurify or similar for sanitization
        element.innerHTML = html;
    }

    /**
     * Toggle class on element
     * @param {HTMLElement} element - Element to toggle class on
     * @param {string} className - Class to toggle
     * @param {boolean} force - Force add/remove
     */
    static toggleClass(element, className, force = null) {
        if (force !== null) {
            element.classList.toggle(className, force);
        } else {
            element.classList.toggle(className);
        }
    }

    /**
     * Find element with error handling
     * @param {string} selector - CSS selector
     * @param {HTMLElement} parent - Parent element (optional)
     * @returns {HTMLElement|null} Found element or null
     */
    static findElement(selector, parent = document) {
        try {
            return parent.querySelector(selector);
        } catch (error) {
            console.warn(`Invalid selector: ${selector}`);
            return null;
        }
    }

    /**
     * Find multiple elements with error handling
     * @param {string} selector - CSS selector
     * @param {HTMLElement} parent - Parent element (optional)
     * @returns {NodeList} Found elements
     */
    static findElements(selector, parent = document) {
        try {
            return parent.querySelectorAll(selector);
        } catch (error) {
            console.warn(`Invalid selector: ${selector}`);
            return [];
        }
    }

    /**
     * Show/hide element
     * @param {HTMLElement} element - Element to show/hide
     * @param {boolean} show - Whether to show element
     */
    static toggleVisibility(element, show) {
        element.style.display = show ? '' : 'none';
    }

    /**
     * Create a loading spinner element
     * @param {string} text - Loading text
     * @returns {HTMLElement} Loading element
     */
    static createLoadingElement(text = 'Loading...') {
        const loading = this.createElement('div', ['loading-spinner']);
        loading.innerHTML = `
            <div class="spinner"></div>
            <span class="loading-text">${text}</span>
        `;
        return loading;
    }

    /**
     * Create an error message element
     * @param {string} message - Error message
     * @param {string} type - Error type (error, warning, success)
     * @returns {HTMLElement} Error element
     */
    static createMessageElement(message, type = 'error') {
        return this.createElement('div', [`message`, `message-${type}`], message);
    }

    /**
     * Debounce function for event handlers
     * @param {Function} func - Function to debounce
     * @param {number} wait - Wait time in milliseconds
     * @param {boolean} immediate - Whether to execute immediately
     * @returns {Function} Debounced function
     */
    static debounce(func, wait, immediate = false) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                timeout = null;
                if (!immediate) func.apply(this, args);
            };
            const callNow = immediate && !timeout;
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
            if (callNow) func.apply(this, args);
        };
    }

    /**
     * Throttle function for event handlers
     * @param {Function} func - Function to throttle
     * @param {number} limit - Time limit in milliseconds
     * @returns {Function} Throttled function
     */
    static throttle(func, limit) {
        let inThrottle;
        return function(...args) {
            if (!inThrottle) {
                func.apply(this, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }
}

export default DOMHelper;