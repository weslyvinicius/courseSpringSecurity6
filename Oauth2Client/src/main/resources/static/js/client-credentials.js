/**
 * Client Credentials Form JavaScript
 * Handles the client credentials flow form interactions
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('Client Credentials form loaded');
    
    // Initialize form
    initializeClientCredentialsForm();
    
    // Setup client selection handlers
    setupClientSelectionHandlers();
    
    // Setup form validation
    setupFormValidation();
    
    // Setup form submission
    setupFormSubmission();
});

/**
 * Initialize the client credentials form
 */
function initializeClientCredentialsForm() {
    console.log('Initializing client credentials form...');
    
    // Pre-select first client if available
    const firstClientRadio = document.querySelector('input[name="selectedClient"]');
    if (firstClientRadio) {
        firstClientRadio.checked = true;
        populateFormFromClient(firstClientRadio);
    }
    
    // Focus on scope field for user input
    const scopeField = document.getElementById('scope');
    if (scopeField) {
        setTimeout(() => scopeField.focus(), 100);
    }
}

/**
 * Setup client selection change handlers
 */
function setupClientSelectionHandlers() {
    const clientRadios = document.querySelectorAll('input[name="selectedClient"]');
    
    clientRadios.forEach(radio => {
        radio.addEventListener('change', function() {
            if (this.checked) {
                console.log('Client selected:', this.dataset.name);
                populateFormFromClient(this);
                validateForm();
            }
        });
    });
}

/**
 * Populate form fields from selected client data
 */
function populateFormFromClient(clientRadio) {
    const clientData = {
        name: clientRadio.dataset.name || '',
        clientId: clientRadio.dataset.clientId || '',
        clientSecret: clientRadio.dataset.clientSecret || '',
        tokenEndpoint: clientRadio.dataset.tokenEndpoint || '',
        scopes: clientRadio.dataset.scopes || ''
    };
    
    console.log('Populating form with client data:', clientData);
    
    // Update form fields
    updateFieldValue('clientName', clientData.name);
    updateFieldValue('clientId', clientData.clientId);
    updateFieldValue('clientSecret', clientData.clientSecret);
    updateFieldValue('tokenEndpoint', clientData.tokenEndpoint);
    updateFieldValue('scope', clientData.scopes);
    
    // Validate after population
    setTimeout(() => validateForm(), 100);
}

/**
 * Update field value safely
 */
function updateFieldValue(fieldId, value) {
    const field = document.getElementById(fieldId);
    if (field && value) {
        field.value = value;
        // Trigger change event for validation
        field.dispatchEvent(new Event('change', { bubbles: true }));
    }
}

/**
 * Setup form validation
 */
function setupFormValidation() {
    const requiredFields = ['clientId', 'clientSecret', 'tokenEndpoint', 'scope'];
    
    requiredFields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (field) {
            // Real-time validation
            field.addEventListener('input', validateForm);
            field.addEventListener('blur', validateForm);
        }
    });
    
    // Initial validation
    validateForm();
}

/**
 * Validate the entire form
 */
function validateForm() {
    let isValid = true;
    const errors = [];
    
    // Check required fields
    const requiredFields = [
        { id: 'clientId', label: 'Client ID' },
        { id: 'clientSecret', label: 'Client Secret' },
        { id: 'tokenEndpoint', label: 'Token Endpoint' },
        { id: 'scope', label: 'Scope' }
    ];
    
    requiredFields.forEach(field => {
        const element = document.getElementById(field.id);
        if (element) {
            const value = element.value.trim();
            
            if (!value) {
                isValid = false;
                errors.push(`${field.label} é obrigatório`);
                addFieldError(element, `${field.label} é obrigatório`);
            } else {
                removeFieldError(element);
                
                // Specific validations
                if (field.id === 'tokenEndpoint' && !isValidUrl(value)) {
                    isValid = false;
                    errors.push('Token Endpoint deve ser uma URL válida');
                    addFieldError(element, 'URL inválida');
                }
            }
        }
    });
    
    // Update submit button state
    const submitBtn = document.getElementById('requestBtn');
    if (submitBtn) {
        submitBtn.disabled = !isValid;
        submitBtn.classList.toggle('disabled', !isValid);
    }
    
    console.log('Form validation:', { isValid, errors });
    return isValid;
}

/**
 * Add error styling to field
 */
function addFieldError(field, message) {
    field.classList.add('error');
    
    // Remove existing error message
    const existingError = field.parentNode.querySelector('.field-error');
    if (existingError) {
        existingError.remove();
    }
    
    // Add error message
    const errorDiv = document.createElement('div');
    errorDiv.className = 'field-error';
    errorDiv.textContent = message;
    field.parentNode.appendChild(errorDiv);
}

/**
 * Remove error styling from field
 */
function removeFieldError(field) {
    field.classList.remove('error');
    
    const errorDiv = field.parentNode.querySelector('.field-error');
    if (errorDiv) {
        errorDiv.remove();
    }
}

/**
 * Validate URL format
 */
function isValidUrl(string) {
    try {
        new URL(string);
        return true;
    } catch (_) {
        return false;
    }
}

/**
 * Setup form submission
 */
function setupFormSubmission() {
    const form = document.getElementById('clientCredentialsForm');
    const submitBtn = document.getElementById('requestBtn');
    const loading = document.getElementById('loading');
    
    if (form && submitBtn) {
        form.addEventListener('submit', function(e) {
            console.log('Form submission started');
            
            // Final validation
            if (!validateForm()) {
                e.preventDefault();
                showNotification('Por favor, corrija os erros no formulário', 'error');
                return;
            }
            
            // Show loading state
            if (loading) {
                loading.style.display = 'flex';
            }
            
            submitBtn.disabled = true;
            submitBtn.innerHTML = '⏳ Solicitando...';
            
            // Log form data
            const formData = new FormData(form);
            console.log('Submitting client credentials request:', {
                clientId: formData.get('clientId'),
                scope: formData.get('scope'),
                tokenEndpoint: formData.get('tokenEndpoint'),
                grantType: formData.get('grantType')
            });
        });
    }
}

/**
 * Show notification message
 */
function showNotification(message, type = 'info') {
    // Remove existing notifications
    const existing = document.querySelector('.notification');
    if (existing) {
        existing.remove();
    }
    
    // Create notification
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <span>${message}</span>
        <button onclick="this.parentNode.remove()" style="background:none;border:none;color:inherit;cursor:pointer;float:right;">×</button>
    `;
    
    // Add to page
    const container = document.querySelector('.main-container');
    if (container) {
        container.insertBefore(notification, container.firstChild);
        
        // Auto-remove after 5 seconds
        setTimeout(() => {
            if (notification && notification.parentNode) {
                notification.remove();
            }
        }, 5000);
    }
}

/**
 * Copy text to clipboard
 */
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        showNotification('Copiado para a área de transferência!', 'success');
    }).catch(err => {
        console.error('Failed to copy: ', err);
        showNotification('Erro ao copiar', 'error');
    });
}

/**
 * Generate random state value
 */
function generateRandomState() {
    return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
}

// Global functions for HTML onclick handlers
window.copyToClipboard = copyToClipboard;
window.showNotification = showNotification;