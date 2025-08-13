/**
 * =============================================================================
 * ENHANCED RESULT PAGE JAVASCRIPT - OAuth2 Client Demo
 * =============================================================================
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize the page
    initializePage();

    // Set up event listeners
    setupEventListeners();

    // Check for authorization code in URL
    checkUrlForAuthCode();

    // Enhance token display
    enhanceTokenDisplay();

    // Process JWT tokens
    processJWTTokens();

    // Analyze permissions
    analyzePermissions();
});

/**
 * Initialize the page with necessary configurations
 */
function initializePage() {
    // Auto-focus client secret field if empty
    const clientSecretField = document.getElementById('tokenClientSecret');
    if (clientSecretField && !clientSecretField.value.trim()) {
        clientSecretField.focus();
    }

    // Show/hide tabs based on available tokens
    const idToken = getTokenValue('id-token-full');
    const refreshToken = getTokenValue('refresh-token-full');

    if (idToken) {
        document.getElementById('id-token-tab-button').style.display = 'block';
    }

    if (refreshToken) {
        document.getElementById('refresh-token-tab-button').style.display = 'block';
    }
}

/**
 * Set up event listeners
 */
function setupEventListeners() {
    // Token form submission
    const tokenForm = document.getElementById('tokenForm');
    if (tokenForm) {
        tokenForm.addEventListener('submit', handleTokenFormSubmit);
    }

    // Tab switching
    document.querySelectorAll('.tab-button').forEach(button => {
        button.addEventListener('click', function() {
            switchTab(this.getAttribute('data-tab'));
        });
    });

    // JWT decode buttons
    document.querySelectorAll('.decode-jwt-btn').forEach(button => {
        button.addEventListener('click', function() {
            const tokenType = this.getAttribute('data-token');
            toggleJWTDecoding(tokenType);
        });
    });

    // Copy buttons (will be added dynamically)
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('copy-button')) {
            const elementId = e.target.getAttribute('data-target');
            copyToClipboard(elementId, e.target);
        }
    });
}

/**
 * Handle token form submission
 */
function handleTokenFormSubmit(e) {
    const loadingElement = document.getElementById('loading');
    const exchangeBtn = document.getElementById('exchangeBtn');

    if (loadingElement) {
        loadingElement.classList.add('show');
    }

    if (exchangeBtn) {
        OAuth2Utils.showButtonLoading('exchangeBtn', 'Processando...');
    }
}

/**
 * Check URL for authorization code and state
 */
function checkUrlForAuthCode() {
    const urlParams = new URLSearchParams(window.location.search);
    const codeFromUrl = urlParams.get('code');
    const stateFromUrl = urlParams.get('state');

    if (codeFromUrl) {
        // Check if backend already provided the code
        const backendCodeElements = document.querySelectorAll('[th\\:text="${authorizationCode}"]');
        const hasBackendCode = Array.from(backendCodeElements).some(el => el.textContent.trim());

        if (!hasBackendCode) {
            showUrlAuthCode(codeFromUrl, stateFromUrl);
        }
    }
}

/**
 * Show authorization code from URL
 */
function showUrlAuthCode(code, state) {
    const urlSection = document.getElementById('url-auth-code-section');
    const urlCodeElement = document.getElementById('url-auth-code');

    if (urlSection && urlCodeElement) {
        urlCodeElement.textContent = code;
        urlSection.style.display = 'block';

        // Add copy button
        addCopyButtonToElement(urlCodeElement, 'url-auth-code');

        // Add state if present
        if (state) {
            const stateContainer = document.getElementById('url-state-container');
            const stateElement = document.getElementById('url-state');
            if (stateContainer && stateElement) {
                stateElement.textContent = state;
                stateContainer.style.display = 'block';
            }
        }
    }
}

/**
 * Switch between tabs
 */
function switchTab(tabId) {
    // Hide all tab contents
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });

    // Remove active class from all tab buttons
    document.querySelectorAll('.tab-button').forEach(button => {
        button.classList.remove('active');
    });

    // Show selected tab
    const targetTab = document.getElementById(tabId);
    if (targetTab) {
        targetTab.classList.add('active');
    }

    // Mark button as active
    const targetButton = document.querySelector(`[data-tab="${tabId}"]`);
    if (targetButton) {
        targetButton.classList.add('active');
    }
}

/**
 * Toggle JWT decoding for a specific token
 */
function toggleJWTDecoding(tokenType) {
    const button = document.querySelector(`[data-token="${tokenType}"]`);
    const decodedSection = document.getElementById(`${tokenType}-decoded`);

    if (!button || !decodedSection) return;

    if (decodedSection.style.display === 'none' || !decodedSection.style.display) {
        // Show and decode
        button.innerHTML = '<div class="decode-spinner"></div>Decodificando...';
        button.disabled = true;

        setTimeout(() => {
            const success = decodeJWTToken(tokenType);

            if (success) {
                decodedSection.style.display = 'block';
                button.textContent = '👁️ Ocultar JWT';
                button.classList.add('active');
            } else {
                button.textContent = '❌ Erro na decodificação';
                setTimeout(() => {
                    button.textContent = '🔍 Decodificar JWT';
                }, 2000);
            }

            button.disabled = false;
        }, 500);
    } else {
        // Hide
        decodedSection.style.display = 'none';
        button.textContent = '🔍 Decodificar JWT';
        button.classList.remove('active');
    }
}

/**
 * Decode JWT token
 */
function decodeJWTToken(tokenType) {
    const token = getTokenValue(`${tokenType}-full`);

    if (!token) {
        console.warn(`Token ${tokenType} não encontrado`);
        return false;
    }

    try {
        const parts = token.split('.');
        if (parts.length !== 3) {
            throw new Error('Token JWT inválido - deve ter 3 partes');
        }

        // Decode header
        const header = JSON.parse(atob(parts[0].replace(/-/g, '+').replace(/_/g, '/')));
        updateJWTSection(`${tokenType}-header`, formatJSON(header));

        // Decode payload
        const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
        updateJWTSection(`${tokenType}-payload`, formatJSON(payload));

        // Signature (base64url encoded)
        updateJWTSection(`${tokenType}-signature`, formatSignature(parts[2]));

        // Add copy buttons
        addCopyButtonsToJWTParts(tokenType);

        return true;
    } catch (error) {
        console.error(`Erro ao decodificar JWT ${tokenType}:`, error);

        const errorMsg = `<div class="jwt-error">Erro ao decodificar: ${error.message}</div>`;
        updateJWTSection(`${tokenType}-header`, errorMsg);
        updateJWTSection(`${tokenType}-payload`, errorMsg);
        updateJWTSection(`${tokenType}-signature`, errorMsg);

        return false;
    }
}

/**
 * Update JWT section content
 */
function updateJWTSection(elementId, content) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = content;
    }
}

/**
 * Format JSON for display
 */
function formatJSON(obj) {
    let formatted = JSON.stringify(obj, null, 2);

    // Apply syntax highlighting
    formatted = formatted
        .replace(/(".*?")\s*:/g, '<span class="json-key">$1</span>:')
        .replace(/:\s*(".*?")/g, ': <span class="json-string">$1</span>')
        .replace(/:\s*(\d+(?:\.\d+)?)/g, ': <span class="json-number">$1</span>')
        .replace(/:\s*(true|false)/g, ': <span class="json-boolean">$1</span>')
        .replace(/:\s*(null)/g, ': <span style="color: #9ca3af;">$1</span>');

    // Format special claims
    formatted = formatSpecialClaims(formatted, obj);

    return `<pre style="margin: 0; white-space: pre-wrap;">${formatted}</pre>`;
}

/**
 * Format special JWT claims (timestamps, etc.)
 */
function formatSpecialClaims(formatted, obj) {
    const timestampFields = ['exp', 'iat', 'nbf', 'auth_time'];

    timestampFields.forEach(field => {
        if (obj[field]) {
            const timestamp = parseInt(obj[field]);
            const date = new Date(timestamp * 1000);
            const dateStr = date.toLocaleString('pt-BR');
            const regex = new RegExp(`("${field}":\\s*)<span class="json-number">(\\d+)</span>`, 'g');
            formatted = formatted.replace(regex,
                `$1<span class="json-number">$2</span> <span style="color: #6b7280; font-size: 0.9em;">(${dateStr})</span>`
            );
        }
    });

    return formatted;
}

/**
 * Format signature section
 */
function formatSignature(signature) {
    return `
        <div style="color: #ed8936; font-weight: bold; margin-bottom: 10px;">
            🔐 Signature (Base64URL encoded)
        </div>
        <div style="
            font-size: 0.85em;
            color: #4a5568;
            word-break: break-all;
            background: #f8f9fa;
            padding: 10px;
            border-radius: 5px;
            border-left: 3px solid #ed8936;
        ">
            ${signature}
        </div>
        <div style="color: #6b7280; font-size: 0.8em; margin-top: 10px; font-style: italic;">
            ℹ️ A assinatura é usada para verificar a autenticidade do token
        </div>
    `;
}

/**
 * Add copy buttons to JWT parts
 */
function addCopyButtonsToJWTParts(tokenType) {
    const sections = ['header', 'payload', 'signature'];
    sections.forEach(section => {
        const element = document.getElementById(`${tokenType}-${section}`);
        if (element && !element.querySelector('.copy-button')) {
            addCopyButtonToElement(element, `${tokenType}-${section}`);
        }
    });
}

/**
 * Get token value from element
 */
function getTokenValue(elementId) {
    const element = document.getElementById(elementId);
    return element ? element.textContent.trim() : null;
}

/**
 * Enhance token display with show/hide functionality
 */
function enhanceTokenDisplay() {
    const tokenElements = document.querySelectorAll('.token-value, .code-value');

    tokenElements.forEach(tokenElement => {
        const text = tokenElement.textContent.trim();

        if (text.length > 100) {
            enhanceLongTokenDisplay(tokenElement, text);
        }

        // Add copy button
        addCopyButtonToElement(tokenElement, null, text);
    });
}

/**
 * Enhance long token display with show/hide
 */
function enhanceLongTokenDisplay(tokenElement, text) {
    const shortText = text.substring(0, 50) + '...';
    const fullText = text;

    tokenElement.innerHTML = `
        <span class="token-short">${shortText}</span>
        <span class="token-full" style="display: none;">${fullText}</span>
        <button class="toggle-token-btn">Mostrar completo</button>
    `;

    const toggleBtn = tokenElement.querySelector('.toggle-token-btn');
    const shortSpan = tokenElement.querySelector('.token-short');
    const fullSpan = tokenElement.querySelector('.token-full');

    toggleBtn.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();

        if (fullSpan.style.display === 'none') {
            shortSpan.style.display = 'none';
            fullSpan.style.display = 'inline';
            this.textContent = 'Mostrar menos';
        } else {
            shortSpan.style.display = 'inline';
            fullSpan.style.display = 'none';
            this.textContent = 'Mostrar completo';
        }
    });
}

/**
 * Add copy button to element
 */
function addCopyButtonToElement(element, elementId, customText = null) {
    if (element.style.position !== 'relative') {
        element.style.position = 'relative';
    }

    const copyBtn = document.createElement('button');
    copyBtn.className = 'copy-button';
    copyBtn.innerHTML = '📋';
    copyBtn.setAttribute('data-target', elementId || 'custom');
    copyBtn.title = 'Copiar para área de transferência';

    if (customText) {
        copyBtn.setAttribute('data-text', customText);
    }

    element.appendChild(copyBtn);
}

/**
 * Process JWT tokens automatically
 */
function processJWTTokens() {
    const tokenTypes = ['access-token', 'id-token', 'refresh-token'];

    tokenTypes.forEach(tokenType => {
        const token = getTokenValue(`${tokenType}-full`);
        if (token && isValidJWT(token)) {
            // Token is available and valid, prepare for decoding
            console.log(`Valid JWT found for ${tokenType}`);
        }
    });
}

/**
 * Check if token is a valid JWT
 */
function isValidJWT(token) {
    return token && token.split('.').length === 3;
}

/**
 * Copy text to clipboard
 */
function copyToClipboard(elementId, button) {
    let textToCopy;

    if (elementId === 'custom') {
        textToCopy = button.getAttribute('data-text');
    } else {
        const element = document.getElementById(elementId);
        if (!element) return;
        textToCopy = element.textContent || element.innerText;
    }

    navigator.clipboard.writeText(textToCopy).then(() => {
        showCopySuccess(button);
    }).catch(err => {
        console.error('Erro ao copiar:', err);

        // Fallback for older browsers
        fallbackCopyTextToClipboard(textToCopy, button);
    });
}

/**
 * Show copy success feedback
 */
function showCopySuccess(button) {
    const originalHTML = button.innerHTML;
    const originalClass = button.className;

    button.innerHTML = '✅';
    button.classList.add('copied');

    setTimeout(() => {
        button.innerHTML = originalHTML;
        button.className = originalClass;
    }, 2000);
}

/**
 * Fallback copy method for older browsers
 */
function fallbackCopyTextToClipboard(text, button) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.top = '0';
    textArea.style.left = '0';
    textArea.style.position = 'fixed';

    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    try {
        const successful = document.execCommand('copy');
        if (successful) {
            showCopySuccess(button);
        } else {
            throw new Error('Copy command failed');
        }
    } catch (err) {
        console.error('Fallback copy failed:', err);
        alert('Não foi possível copiar automaticamente. Por favor, selecione e copie manualmente.');
    }

    document.body.removeChild(textArea);
}

/**
 * Analyze permissions (scopes vs authorities)
 */
function analyzePermissions() {
    const analysisContent = document.getElementById('permissions-analysis-content');
    if (!analysisContent) return;

    const accessToken = getTokenValue('access-token-full');
    if (!accessToken || !isValidJWT(accessToken)) {
        analysisContent.innerHTML = '<p class="jwt-error">Nenhum token de acesso válido disponível para análise</p>';
        return;
    }

    try {
        const payload = JSON.parse(atob(accessToken.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
        generatePermissionsAnalysis(payload, analysisContent);
    } catch (error) {
        console.error('Erro ao analisar permissões:', error);
        analysisContent.innerHTML = `<p class="jwt-error">Erro ao analisar permissões: ${error.message}</p>`;
    }
}

/**
 * Generate permissions analysis table
 */
function generatePermissionsAnalysis(payload, container) {
    const scopeMapping = {
        'employee.read': ['READ_EMPLOYEE'],
        'employee.write': ['CREATE_EMPLOYEE', 'UPDATE_EMPLOYEE'],
        'employee.delete': ['DELETE_EMPLOYEE'],
        'report.read': ['READ_REPORT'],
        'report.write': ['CREATE_REPORT', 'UPDATE_REPORT'],
        'report.delete': ['DELETE_REPORT'],
        'admin': ['ROLE_ADMIN'],
        'openid': ['OPENID'],
        'profile': ['PROFILE'],
        'email': ['EMAIL']
    };

    const userAuthorities = payload.authorities || [];
    let scopes = extractScopes(payload);

    if (scopes.length === 0) {
        container.innerHTML = '<p class="jwt-error">⚠️ Nenhum escopo encontrado no token</p>';
        return;
    }

    let tableHTML = `
        <table class="permissions-table">
            <thead>
                <tr>
                    <th>Escopo Solicitado</th>
                    <th>Authority Necessária</th>
                    <th>Usuário Possui?</th>
                    <th>No Token?</th>
                </tr>
            </thead>
            <tbody>
    `;

    scopes.forEach(scope => {
        const requiredAuthorities = scopeMapping[scope] || [`SCOPE_${scope.toUpperCase()}`];

        requiredAuthorities.forEach(authority => {
            const hasAuthority = userAuthorities.includes(authority);
            const inToken = hasAuthority; // Simplified logic

            tableHTML += `
                <tr>
                    <td><code>${scope}</code></td>
                    <td><code>${authority}</code></td>
                    <td><span class="${hasAuthority ? 'status-granted' : 'status-denied'}">
                        ${hasAuthority ? '✅ Sim' : '❌ Não'}
                    </span></td>
                    <td><span class="${inToken ? 'status-granted' : 'status-denied'}">
                        ${inToken ? '✅ Sim' : '❌ Não'}
                    </span></td>
                </tr>
            `;
        });
    });

    tableHTML += '</tbody></table>';

    // Add summary
    const grantedCount = userAuthorities.length;
    const totalScopes = scopes.length;

    tableHTML += `
        <div style="margin-top: 15px; padding: 10px; background: #f8f9fa; border-radius: 8px;">
            <strong>📊 Resumo:</strong>
            ${grantedCount} authorities concedidas de ${totalScopes} escopos solicitados
        </div>
    `;

    container.innerHTML = tableHTML;
}

/**
 * Extract scopes from token payload
 */
function extractScopes(payload) {
    let scopes = [];

    // Try different scope fields
    if (payload.scope) {
        if (typeof payload.scope === 'string') {
            scopes = payload.scope.split(' ');
        } else if (Array.isArray(payload.scope)) {
            scopes = payload.scope;
        }
    }

    if (payload.scp && Array.isArray(payload.scp)) {
        scopes = [...scopes, ...payload.scp];
    }

    // Remove duplicates and empty strings
    return [...new Set(scopes)].filter(scope => scope && scope.trim());
}

/**
 * Test token with JWT.io
 */
function testWithJwtIo() {
    const accessToken = getTokenValue('access-token-full');

    if (accessToken) {
        const jwtIoUrl = 'https://jwt.io/#debugger-io?token=' + encodeURIComponent(accessToken);
        window.open(jwtIoUrl, '_blank');
    } else {
        alert('Nenhum token de acesso disponível para verificar!');
    }
}

/**
 * Copy all tokens
 */
function copyAllTokens() {
    const tokens = {
        accessToken: getTokenValue('access-token-full'),
        idToken: getTokenValue('id-token-full'),
        refreshToken: getTokenValue('refresh-token-full')
    };

    const availableTokens = Object.entries(tokens)
        .filter(([key, value]) => value && value.trim())
        .map(([key, value]) => `${key}: ${value}`)
        .join('\n\n');

    if (availableTokens) {
        const fullText = `OAuth2 Tokens:\n\n${availableTokens}`;

        navigator.clipboard.writeText(fullText).then(() => {
            showTemporaryMessage('Todos os tokens copiados!', 'success');
        }).catch(err => {
            console.error('Erro ao copiar tokens:', err);
            showTemporaryMessage('Erro ao copiar tokens', 'error');
        });
    } else {
        showTemporaryMessage('Nenhum token disponível para copiar', 'warning');
    }
}

/**
 * Show temporary message
 */
function showTemporaryMessage(message, type = 'info') {
    const messageDiv = document.createElement('div');
    messageDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 25px;
        border-radius: 8px;
        color: white;
        font-weight: bold;
        z-index: 1000;
        animation: slideInRight 0.3s ease-out;
    `;

    // Set background color based on type
    const colors = {
        success: '#38a169',
        error: '#e53e3e',
        warning: '#d69e2e',
        info: '#4299e1'
    };

    messageDiv.style.background = colors[type] || colors.info;
    messageDiv.textContent = message;

    // Add animation styles
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideInRight {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
        @keyframes slideOutRight {
            from { transform: translateX(0); opacity: 1; }
            to { transform: translateX(100%); opacity: 0; }
        }
    `;
    document.head.appendChild(style);

    document.body.appendChild(messageDiv);

    // Remove after 3 seconds
    setTimeout(() => {
        messageDiv.style.animation = 'slideOutRight 0.3s ease-out';
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.parentNode.removeChild(messageDiv);
            }
            if (style.parentNode) {
                style.parentNode.removeChild(style);
            }
        }, 300);
    }, 3000);
}

/**
 * Export token data as JSON
 */
function exportTokenData() {
    const tokens = {
        accessToken: getTokenValue('access-token-full'),
        idToken: getTokenValue('id-token-full'),
        refreshToken: getTokenValue('refresh-token-full')
    };

    // Decode tokens if they're JWTs
    const tokenData = {
        timestamp: new Date().toISOString(),
        tokens: {}
    };

    Object.entries(tokens).forEach(([key, token]) => {
        if (token && isValidJWT(token)) {
            try {
                const parts = token.split('.');
                const header = JSON.parse(atob(parts[0].replace(/-/g, '+').replace(/_/g, '/')));
                const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));

                tokenData.tokens[key] = {
                    raw: token,
                    header: header,
                    payload: payload,
                    signature: parts[2]
                };
            } catch (error) {
                tokenData.tokens[key] = {
                    raw: token,
                    error: 'Failed to decode JWT: ' + error.message
                };
            }
        } else if (token) {
            tokenData.tokens[key] = {
                raw: token,
                type: 'non-JWT'
            };
        }
    });

    // Create download
    const blob = new Blob([JSON.stringify(tokenData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `oauth2-tokens-${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);

    showTemporaryMessage('Dados dos tokens exportados!', 'success');
}

/**
 * Validate token expiration
 */
function validateTokenExpiration() {
    const accessToken = getTokenValue('access-token-full');

    if (!accessToken || !isValidJWT(accessToken)) {
        return null;
    }

    try {
        const payload = JSON.parse(atob(accessToken.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
        const now = Math.floor(Date.now() / 1000);

        const result = {
            isExpired: false,
            expiresAt: null,
            timeUntilExpiry: null,
            issuedAt: null,
            notBefore: null
        };

        if (payload.exp) {
            result.expiresAt = new Date(payload.exp * 1000);
            result.isExpired = now > payload.exp;
            result.timeUntilExpiry = payload.exp - now;
        }

        if (payload.iat) {
            result.issuedAt = new Date(payload.iat * 1000);
        }

        if (payload.nbf) {
            result.notBefore = new Date(payload.nbf * 1000);
        }

        return result;
    } catch (error) {
        console.error('Erro ao validar expiração do token:', error);
        return null;
    }
}

/**
 * Show token expiration info
 */
function showTokenExpirationInfo() {
    const expInfo = validateTokenExpiration();

    if (!expInfo) {
        showTemporaryMessage('Não foi possível validar a expiração do token', 'warning');
        return;
    }

    let message = '';
    if (expInfo.isExpired) {
        message = `⚠️ Token expirou em ${expInfo.expiresAt.toLocaleString('pt-BR')}`;
        showTemporaryMessage(message, 'error');
    } else if (expInfo.timeUntilExpiry) {
        const hours = Math.floor(expInfo.timeUntilExpiry / 3600);
        const minutes = Math.floor((expInfo.timeUntilExpiry % 3600) / 60);
        message = `✅ Token válido por mais ${hours}h ${minutes}m`;
        showTemporaryMessage(message, 'success');
    } else {
        showTemporaryMessage('Token não possui informação de expiração', 'info');
    }
}

/**
 * Add token expiration monitoring
 */
function setupTokenExpirationMonitoring() {
    const expInfo = validateTokenExpiration();

    if (!expInfo || !expInfo.timeUntilExpiry) {
        return;
    }

    // Show warning when token is about to expire (5 minutes)
    const warningTime = expInfo.timeUntilExpiry - 300; // 5 minutes before

    if (warningTime > 0) {
        setTimeout(() => {
            showTemporaryMessage('⚠️ Token expirará em 5 minutos', 'warning');
        }, warningTime * 1000);
    }

    // Show expiration notice
    setTimeout(() => {
        showTemporaryMessage('❌ Token expirou', 'error');

        // Add expired class to token sections
        document.querySelectorAll('.token-response-section').forEach(section => {
            section.style.opacity = '0.6';
            section.style.border = '2px solid #e53e3e';
        });
    }, expInfo.timeUntilExpiry * 1000);
}

/**
 * Initialize advanced features
 */
function initializeAdvancedFeatures() {
    // Add export button if tokens are available
    const tokenResponseSection = document.querySelector('.token-response-section');
    if (tokenResponseSection) {
        const exportBtn = document.createElement('button');
        exportBtn.className = 'btn btn-secondary';
        exportBtn.innerHTML = '📥 Exportar Dados';
        exportBtn.onclick = exportTokenData;

        const actionsDiv = document.querySelector('.token-actions');
        if (actionsDiv) {
            actionsDiv.appendChild(exportBtn);
        }

        // Add expiration info button
        const expBtn = document.createElement('button');
        expBtn.className = 'btn btn-info';
        expBtn.innerHTML = '⏰ Verificar Expiração';
        expBtn.onclick = showTokenExpirationInfo;
        actionsDiv.appendChild(expBtn);

        // Setup monitoring
        setupTokenExpirationMonitoring();
    }
}

/**
 * Enhanced error handling
 */
function handleJWTDecodeError(tokenType, error) {
    console.error(`Erro ao decodificar JWT ${tokenType}:`, error);

    const errorTypes = {
        'Invalid token': 'Token inválido - verifique se está completo',
        'Invalid JSON': 'Formato JSON inválido no token',
        'Invalid base64': 'Codificação Base64 inválida'
    };

    const userFriendlyError = errorTypes[error.message] || error.message;

    const errorHTML = `
        <div class="jwt-error">
            <strong>❌ Erro de Decodificação</strong><br>
            ${userFriendlyError}<br>
            <small>Detalhes técnicos: ${error.message}</small>
        </div>
    `;

    ['header', 'payload', 'signature'].forEach(section => {
        updateJWTSection(`${tokenType}-${section}`, errorHTML);
    });
}

/**
 * Add keyboard shortcuts
 */
function setupKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + J: Open JWT.io
        if ((e.ctrlKey || e.metaKey) && e.key === 'j') {
            e.preventDefault();
            testWithJwtIo();
        }

        // Ctrl/Cmd + C: Copy all tokens (when focused on token area)
        if ((e.ctrlKey || e.metaKey) && e.key === 'c' && e.target.closest('.token-response-section')) {
            if (!window.getSelection().toString()) {
                e.preventDefault();
                copyAllTokens();
            }
        }

        // Ctrl/Cmd + E: Export data
        if ((e.ctrlKey || e.metaKey) && e.key === 'e') {
            e.preventDefault();
            exportTokenData();
        }
    });
}

/**
 * Initialize everything when DOM is loaded
 */
document.addEventListener('DOMContentLoaded', function() {
    // Run initial setup with delay to ensure DOM is fully ready
    setTimeout(() => {
        initializeAdvancedFeatures();
        setupKeyboardShortcuts();
    }, 100);
});

/**
 * Add utility functions to window object for debugging
 */
if (typeof window !== 'undefined') {
    window.OAuth2Debug = {
        getTokenValue,
        decodeJWTToken,
        validateTokenExpiration,
        analyzePermissions,
        isValidJWT,
        exportTokenData
    };
}