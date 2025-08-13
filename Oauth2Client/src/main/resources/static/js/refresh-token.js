// =============================================================================
// REFRESH TOKEN PAGE JAVASCRIPT - OAuth2 Client Demo
// =============================================================================

document.addEventListener('DOMContentLoaded', function() {
    initializeRefreshTokenPage();
});

function initializeRefreshTokenPage() {
    console.log('Initializing Refresh Token page...');
    
    // Setup client selector
    setupClientSelector();
    
    // Setup form validation
    setupFormValidation();
    
    // Setup form submission
    setupFormSubmission();
    
    // Setup token validation
    setupTokenValidation();
    
    console.log('Refresh Token page initialized successfully');
}

function setupClientSelector() {
    const clientOptions = document.querySelectorAll('input[name="selectedClient"]');
    
    clientOptions.forEach(option => {
        option.addEventListener('change', function() {
            if (this.checked) {
                updateFormWithClientData(this);
            }
        });
    });
    
    // Auto-select first client if available
    if (clientOptions.length > 0) {
        updateFormWithClientData(clientOptions[0]);
    }
}

function updateFormWithClientData(clientOption) {
    console.log('Updating form with client data:', clientOption.dataset);
    
    const elements = {
        clientName: document.getElementById('clientName'),
        clientId: document.getElementById('clientId'),
        clientSecret: document.getElementById('clientSecret'),
        tokenEndpoint: document.getElementById('tokenEndpoint'),
        scope: document.getElementById('scope')
    };
    
    // Update form fields with client data
    if (elements.clientName) elements.clientName.value = clientOption.dataset.name || '';
    if (elements.clientId) elements.clientId.value = clientOption.dataset.clientId || '';
    if (elements.clientSecret) elements.clientSecret.value = clientOption.dataset.clientSecret || '';
    if (elements.tokenEndpoint) elements.tokenEndpoint.value = clientOption.dataset.tokenEndpoint || '';
    if (elements.scope) elements.scope.value = clientOption.dataset.scopes || '';
    
    // Visual feedback
    const clientSelector = document.querySelector('.client-selector');
    if (clientSelector) {
        clientSelector.classList.add('updated');
        setTimeout(() => clientSelector.classList.remove('updated'), 500);
    }
}

function setupFormValidation() {
    const form = document.getElementById('refreshTokenForm');
    const refreshTokenInput = document.getElementById('refreshToken');
    const clientIdInput = document.getElementById('clientId');
    const clientSecretInput = document.getElementById('clientSecret');
    const tokenEndpointInput = document.getElementById('tokenEndpoint');
    
    // Real-time validation for refresh token
    if (refreshTokenInput) {
        refreshTokenInput.addEventListener('input', function() {
            validateRefreshToken(this.value.trim());
        });
        
        refreshTokenInput.addEventListener('paste', function(e) {
            setTimeout(() => {
                validateRefreshToken(this.value.trim());
            }, 100);
        });
    }
    
    // URL validation for token endpoint
    if (tokenEndpointInput) {
        tokenEndpointInput.addEventListener('input', function() {
            validateURL(this, 'Token endpoint inválido');
        });
    }
    
    // Required field validation
    [clientIdInput, clientSecretInput, tokenEndpointInput, refreshTokenInput].forEach(input => {
        if (input) {
            input.addEventListener('blur', function() {
                validateRequiredField(this);
            });
        }
    });
}

function validateRefreshToken(token) {
    const input = document.getElementById('refreshToken');
    const isValid = token.length > 10; // Basic length check
    
    if (input) {
        input.classList.toggle('valid', isValid && token.length > 0);
        input.classList.toggle('invalid', !isValid && token.length > 0);
        
        // Show/hide validation message
        let validationMsg = input.parentElement.querySelector('.validation-message');
        if (!validationMsg) {
            validationMsg = document.createElement('small');
            validationMsg.className = 'validation-message';
            input.parentElement.appendChild(validationMsg);
        }
        
        if (token.length === 0) {
            validationMsg.textContent = '';
            validationMsg.className = 'validation-message';
        } else if (isValid) {
            validationMsg.textContent = '✓ Refresh token válido';
            validationMsg.className = 'validation-message success';
        } else {
            validationMsg.textContent = '⚠ Refresh token parece muito curto';
            validationMsg.className = 'validation-message error';
        }
    }
    
    return isValid;
}

function validateURL(input, errorMessage) {
    try {
        const url = new URL(input.value);
        input.classList.remove('invalid');
        input.classList.add('valid');
        hideValidationError(input);
        return true;
    } catch (e) {
        if (input.value.trim() !== '') {
            input.classList.remove('valid');
            input.classList.add('invalid');
            showValidationError(input, errorMessage);
        }
        return false;
    }
}

function validateRequiredField(input) {
    const isEmpty = input.value.trim() === '';
    
    input.classList.toggle('invalid', isEmpty);
    input.classList.toggle('valid', !isEmpty);
    
    if (isEmpty) {
        showValidationError(input, 'Campo obrigatório');
    } else {
        hideValidationError(input);
    }
    
    return !isEmpty;
}

function showValidationError(input, message) {
    let errorElement = input.parentElement.querySelector('.validation-error');
    if (!errorElement) {
        errorElement = document.createElement('small');
        errorElement.className = 'validation-error';
        input.parentElement.appendChild(errorElement);
    }
    errorElement.textContent = message;
}

function hideValidationError(input) {
    const errorElement = input.parentElement.querySelector('.validation-error');
    if (errorElement) {
        errorElement.remove();
    }
}

function setupFormSubmission() {
    const form = document.getElementById('refreshTokenForm');
    const submitBtn = document.getElementById('refreshBtn');
    const loading = document.getElementById('loading');
    
    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            console.log('Form submission started');
            
            // Validate form before submission
            if (!validateForm()) {
                showNotification('Por favor, corrija os erros no formulário', 'error');
                return;
            }
            
            // Show loading state
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '🔄 Renovando...';
            }
            
            if (loading) {
                loading.style.display = 'flex';
            }
            
            // Log form data for debugging
            const formData = new FormData(form);
            console.log('Form data being submitted:');
            for (let [key, value] of formData.entries()) {
                if (key === 'clientSecret' || key === 'refreshToken') {
                    console.log(`${key}: [HIDDEN]`);
                } else {
                    console.log(`${key}: ${value}`);
                }
            }
            
            // Submit form
            form.submit();
        });
    }
}

function validateForm() {
    const requiredFields = [
        'clientId',
        'clientSecret', 
        'refreshToken',
        'tokenEndpoint'
    ];
    
    let isValid = true;
    
    requiredFields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (field && !validateRequiredField(field)) {
            isValid = false;
        }
    });
    
    // Validate token endpoint URL
    const tokenEndpoint = document.getElementById('tokenEndpoint');
    if (tokenEndpoint && !validateURL(tokenEndpoint, 'URL inválida')) {
        isValid = false;
    }
    
    // Validate refresh token
    const refreshToken = document.getElementById('refreshToken');
    if (refreshToken && !validateRefreshToken(refreshToken.value.trim())) {
        isValid = false;
    }
    
    return isValid;
}

function setupTokenValidation() {
    // Add token format detection
    const refreshTokenInput = document.getElementById('refreshToken');
    
    if (refreshTokenInput) {
        refreshTokenInput.addEventListener('input', function() {
            const token = this.value.trim();
            detectTokenFormat(token);
        });
    }
}

function detectTokenFormat(token) {
    const input = document.getElementById('refreshToken');
    let formatInfo = input.parentElement.querySelector('.token-format-info');
    
    if (!formatInfo) {
        formatInfo = document.createElement('div');
        formatInfo.className = 'token-format-info';
        input.parentElement.appendChild(formatInfo);
    }
    
    if (token.length === 0) {
        formatInfo.innerHTML = '';
        return;
    }
    
    if (token.startsWith('ey')) {
        formatInfo.innerHTML = '<span class="format-badge jwt">JWT</span> Token JWT detectado';
    } else if (token.length > 50) {
        formatInfo.innerHTML = '<span class="format-badge opaque">Opaque</span> Token opaco detectado';
    } else if (token.length > 0) {
        formatInfo.innerHTML = '<span class="format-badge unknown">?</span> Formato de token não identificado';
    }
}

// Utility functions
function showNotification(message, type = 'info') {
    // Create or update notification
    let notification = document.querySelector('.notification');
    if (!notification) {
        notification = document.createElement('div');
        notification.className = 'notification';
        document.body.appendChild(notification);
    }
    
    notification.textContent = message;
    notification.className = `notification ${type} show`;
    
    setTimeout(() => {
        notification.classList.remove('show');
    }, 5000);
}

// Auto-fill from localStorage if available (for development)
function loadDevelopmentDefaults() {
    if (localStorage.getItem('oauth2-dev-mode') === 'true') {
        const savedRefreshToken = localStorage.getItem('oauth2-last-refresh-token');
        if (savedRefreshToken) {
            const refreshTokenInput = document.getElementById('refreshToken');
            if (refreshTokenInput) {
                refreshTokenInput.value = savedRefreshToken;
                validateRefreshToken(savedRefreshToken);
            }
        }
    }
}

// Call on page load
document.addEventListener('DOMContentLoaded', loadDevelopmentDefaults);