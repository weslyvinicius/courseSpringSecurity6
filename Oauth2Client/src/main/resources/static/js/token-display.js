/* =============================================================================
   TOKEN DISPLAY SCRIPTS - OAuth2 Client Demo
   ============================================================================= */

// Função para verificar se um elemento existe antes de manipulá-lo
function safeGetElement(id) {
    const element = document.getElementById(id);
    if (!element) {
        console.warn(`Elemento com ID '${id}' não encontrado`);
    }
    return element;
}

// Função para verificar se um token é um JWT válido
function isValidJWT(token) {
    if (!token || typeof token !== 'string') return false;
    const parts = token.split('.');
    return parts.length === 3 && parts[0].length > 0 && parts[1].length > 0;
}

// Função para obter o valor de um token de forma segura
function getTokenValue(elementId) {
    const element = safeGetElement(elementId);
    if (!element) return null;
    const token = element.textContent || element.innerText;
    return token && token.trim() !== '' ? token.trim() : null;
}

// Toggle individual token details com verificações de segurança
function toggleTokenDetails(tokenType) {
    const panel = safeGetElement(tokenType + '-details');
    const content = safeGetElement(tokenType + '-content');

    if (!panel || !content) {
        console.warn(`Elementos do token '${tokenType}' não encontrados`);
        return;
    }

    const icon = panel.querySelector('.toggle-icon');

    if (panel.classList.contains('expanded')) {
        panel.classList.remove('expanded');
        content.style.display = 'none';
        if (icon) icon.textContent = '➕';
    } else {
        panel.classList.add('expanded');
        content.style.display = 'block';
        if (icon) icon.textContent = '➖';

        // Auto-decode JWT if not already done
        const token = getTokenValue(tokenType + '-full');
        if (token && isValidJWT(token)) {
            const decodedSection = safeGetElement(tokenType + '-decoded');
            if (decodedSection && decodedSection.style.display === 'none') {
                decodeJWTToken(tokenType);
                decodedSection.style.display = 'block';
            }
        }
    }
}

// Toggle all token details com verificações de segurança
function toggleAllTokenDetails() {
    const panels = document.querySelectorAll('.token-detail-panel');
    const toggleText = safeGetElement('toggle-all-text');

    if (panels.length === 0) {
        console.warn('Nenhum painel de token encontrado');
        return;
    }

    const anyExpanded = Array.from(panels).some(panel => panel.classList.contains('expanded'));

    panels.forEach(panel => {
        const tokenType = panel.id.replace('-details', '');

        if (anyExpanded) {
            // Close all
            panel.classList.remove('expanded');
            const content = panel.querySelector('.token-detail-content');
            if (content) content.style.display = 'none';
            const icon = panel.querySelector('.toggle-icon');
            if (icon) icon.textContent = '➕';
        } else {
            // Open all
            panel.classList.add('expanded');
            const content = panel.querySelector('.token-detail-content');
            if (content) {
                content.style.display = 'block';

                // Auto-decode JWT
                const token = getTokenValue(tokenType + '-full');
                if (token && isValidJWT(token)) {
                    const decodedSection = safeGetElement(tokenType + '-decoded');
                    if (decodedSection && decodedSection.style.display === 'none') {
                        decodeJWTToken(tokenType);
                        decodedSection.style.display = 'block';
                    }
                }
            }
            const icon = panel.querySelector('.toggle-icon');
            if (icon) icon.textContent = '➖';
        }
    });

    if (toggleText) {
        toggleText.textContent = anyExpanded ? '👁️ Mostrar Todos' : '🙈 Ocultar Todos';
    }
}

// Decodificar JWT Token com tratamento de erros
function decodeJWTToken(tokenType) {
    const token = getTokenValue(tokenType + '-full');

    if (!token || !isValidJWT(token)) {
        console.warn(`Token '${tokenType}' inválido ou não encontrado`);
        return;
    }

    try {
        const parts = token.split('.');

        // Decode header
        const headerElement = safeGetElement(tokenType + '-header');
        if (headerElement) {
            try {
                const header = JSON.parse(atob(parts[0].replace(/-/g, '+').replace(/_/g, '/')));
                headerElement.textContent = JSON.stringify(header, null, 2);
            } catch (e) {
                headerElement.textContent = 'Erro ao decodificar header: ' + e.message;
            }
        }

        // Decode payload
        const payloadElement = safeGetElement(tokenType + '-payload');
        if (payloadElement) {
            try {
                const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
                payloadElement.textContent = JSON.stringify(payload, null, 2);
            } catch (e) {
                payloadElement.textContent = 'Erro ao decodificar payload: ' + e.message;
            }
        }

        // Show signature
        const signatureElement = safeGetElement(tokenType + '-signature');
        if (signatureElement) {
            signatureElement.textContent = parts[2] || 'Signature não disponível';
        }

    } catch (error) {
        console.error(`Erro ao decodificar token '${tokenType}':`, error);

        // Mostrar erro nos elementos
        ['-header', '-payload', '-signature'].forEach(suffix => {
            const element = safeGetElement(tokenType + suffix);
            if (element) {
                element.textContent = 'Erro ao decodificar: ' + error.message;
            }
        });
    }
}

// Generate token claims preview com verificações de segurança
function generateTokenClaimsPreview(tokenType) {
    const token = getTokenValue(tokenType + '-full');
    const previewElement = safeGetElement(tokenType + '-claims-preview');

    if (!token || !isValidJWT(token) || !previewElement) return;

    try {
        const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));

        let preview = [];

        // Show key claims based on token type
        if (tokenType === 'access-token') {
            if (payload.scope) preview.push(`Scopes: ${payload.scope}`);
            if (payload.aud) preview.push(`Audience: ${Array.isArray(payload.aud) ? payload.aud.join(', ') : payload.aud}`);
            if (payload.exp) {
                const expDate = new Date(payload.exp * 1000);
                preview.push(`Expira: ${expDate.toLocaleTimeString('pt-BR')}`);
            }
        } else if (tokenType === 'id-token') {
            if (payload.name) preview.push(`Nome: ${payload.name}`);
            if (payload.email) preview.push(`Email: ${payload.email}`);
            if (payload.sub) preview.push(`Subject: ${payload.sub.substring(0, 20)}...`);
        } else if (tokenType === 'refresh-token') {
            if (payload.aud) preview.push(`Audience: ${Array.isArray(payload.aud) ? payload.aud.join(', ') : payload.aud}`);
            if (payload.scope) preview.push(`Scopes: ${payload.scope}`);
        }

        previewElement.innerHTML = preview.length > 0
            ? preview.join(' • ')
            : 'Claims padrão disponíveis';

    } catch (error) {
        console.warn(`Erro ao gerar preview para '${tokenType}':`, error);
        previewElement.innerHTML = 'Erro ao gerar preview';
    }
}

// Função para copiar texto para área de transferência
function copyToClipboard(text, button, altText = null) {
    const textToCopy = altText || text;

    if (!textToCopy) {
        console.warn('Nenhum texto para copiar');
        return;
    }

    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(textToCopy).then(() => {
            showCopyFeedback(button, true);
        }).catch(err => {
            console.error('Erro ao copiar:', err);
            fallbackCopyTextToClipboard(textToCopy, button);
        });
    } else {
        fallbackCopyTextToClipboard(textToCopy, button);
    }
}

// Fallback para copiar texto
function fallbackCopyTextToClipboard(text, button) {
    const textArea = document.createElement("textarea");
    textArea.value = text;
    textArea.style.top = "0";
    textArea.style.left = "0";
    textArea.style.position = "fixed";
    textArea.style.opacity = "0";

    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    try {
        const successful = document.execCommand('copy');
        showCopyFeedback(button, successful);
    } catch (err) {
        console.error('Fallback: Erro ao copiar:', err);
        showCopyFeedback(button, false);
    }

    document.body.removeChild(textArea);
}

// Mostrar feedback da cópia
function showCopyFeedback(button, success) {
    if (!button) return;

    const originalText = button.textContent;
    const originalBackground = button.style.background;

    if (success) {
        button.textContent = '✅ Copiado!';
        button.style.background = '#38a169';
    } else {
        button.textContent = '❌ Erro';
        button.style.background = '#e53e3e';
    }

    setTimeout(() => {
        button.textContent = originalText;
        button.style.background = originalBackground;
    }, 2000);
}

// Funções dos botões de ação rápida
function testWithJwtIo() {
    const accessToken = getTokenValue('access-token-full');
    const idToken = getTokenValue('id-token-full');

    const token = accessToken || idToken;

    if (token && isValidJWT(token)) {
        const url = `https://jwt.io/#debugger-io?token=${encodeURIComponent(token)}`;
        window.open(url, '_blank');
    } else {
        alert('Nenhum JWT válido encontrado para verificar.');
    }
}

function copyAllTokens() {
    const tokens = [];

    ['access-token', 'id-token', 'refresh-token'].forEach(tokenType => {
        const token = getTokenValue(tokenType + '-full');
        if (token) {
            tokens.push(`${tokenType.toUpperCase().replace('-', '_')}: ${token}`);
        }
    });

    if (tokens.length > 0) {
        copyToClipboard(tokens.join('\n\n'));
        alert(`${tokens.length} token(s) copiado(s) para a área de transferência!`);
    } else {
        alert('Nenhum token encontrado para copiar.');
    }
}

function exportTokenData() {
    const data = {};

    ['access-token', 'id-token', 'refresh-token'].forEach(tokenType => {
        const token = getTokenValue(tokenType + '-full');
        if (token) {
            const key = tokenType.replace('-', '_');
            data[key] = {
                token: token,
                type: tokenType,
                is_jwt: isValidJWT(token),
                exported_at: new Date().toISOString()
            };

            if (isValidJWT(token)) {
                try {
                    const parts = token.split('.');
                    data[key].decoded = {
                        header: JSON.parse(atob(parts[0].replace(/-/g, '+').replace(/_/g, '/'))),
                        payload: JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'))),
                        signature: parts[2]
                    };
                } catch (e) {
                    data[key].decode_error = e.message;
                }
            }
        }
    });

    if (Object.keys(data).length === 0) {
        alert('Nenhum token encontrado para exportar.');
        return;
    }

    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `oauth2_tokens_${new Date().toISOString().split('T')[0]}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

function showTokenExpirationInfo() {
    const expirationInfo = [];

    ['access-token', 'id-token', 'refresh-token'].forEach(tokenType => {
        const token = getTokenValue(tokenType + '-full');
        if (token && isValidJWT(token)) {
            try {
                const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
                const tokenName = tokenType.replace('-', ' ').toUpperCase();

                if (payload.exp) {
                    const expDate = new Date(payload.exp * 1000);
                    const now = new Date();
                    const timeLeft = expDate.getTime() - now.getTime();

                    if (timeLeft > 0) {
                        const hoursLeft = Math.floor(timeLeft / (1000 * 60 * 60));
                        const minutesLeft = Math.floor((timeLeft % (1000 * 60 * 60)) / (1000 * 60));
                        expirationInfo.push(`${tokenName}: ${hoursLeft}h ${minutesLeft}m restantes`);
                    } else {
                        expirationInfo.push(`${tokenName}: ⚠️ EXPIRADO`);
                    }
                } else {
                    expirationInfo.push(`${tokenName}: Sem informação de expiração`);
                }
            } catch (e) {
                expirationInfo.push(`${tokenType.replace('-', ' ').toUpperCase()}: Erro ao verificar expiração`);
            }
        }
    });

    if (expirationInfo.length > 0) {
        alert('⏰ Informações de Expiração:\n\n' + expirationInfo.join('\n'));
    } else {
        alert('Nenhum token JWT encontrado para verificar expiração.');
    }
}

// Initialize token display when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    console.log('Inicializando token display...');

    // Set current time for metadata
    const timeElement = safeGetElement('token-received-time');
    if (timeElement) {
        timeElement.textContent = new Date().toLocaleString('pt-BR');
    }

    // Generate claims previews only for existing tokens
    ['access-token', 'id-token', 'refresh-token'].forEach(tokenType => {
        const tokenElement = safeGetElement(tokenType + '-full');
        if (tokenElement && tokenElement.textContent.trim()) {
            generateTokenClaimsPreview(tokenType);
        }
    });

    // Setup copy buttons with error handling
    document.querySelectorAll('[data-text]').forEach(button => {
        button.addEventListener('click', function() {
            const text = this.getAttribute('data-text');
            if (text && text.trim() !== '') {
                copyToClipboard(null, this, text);
            } else {
                console.warn('Botão de cópia sem texto válido');
                showCopyFeedback(this, false);
            }
        });
    });

    // Setup decode JWT buttons
    document.querySelectorAll('.decode-jwt-btn').forEach(button => {
        button.addEventListener('click', function() {
            const tokenType = this.getAttribute('data-token');
            if (tokenType) {
                const token = getTokenValue(tokenType + '-full');
                if (token && isValidJWT(token)) {
                    decodeJWTToken(tokenType);

                    // Show decoded section
                    const decodedSection = safeGetElement(tokenType + '-decoded');
                    if (decodedSection) {
                        decodedSection.style.display = 'block';
                    }

                    // Update button
                    this.textContent = '✅ Decodificado';
                    this.disabled = true;
                } else {
                    alert('Token inválido ou não é um JWT');
                }
            }
        });
    });

    // Setup copy code buttons
    document.querySelectorAll('.copy-code-btn').forEach(button => {
        button.addEventListener('click', function() {
            const targetId = this.getAttribute('data-target');
            if (targetId) {
                const text = getTokenValue(targetId);
                if (text) {
                    copyToClipboard(null, this, text);
                } else {
                    showCopyFeedback(this, false);
                }
            }
        });
    });

    console.log('Token display inicializado com sucesso!');
});

// Function to save refresh token for development mode
function saveRefreshTokenForDev() {
    if (localStorage.getItem('oauth2-dev-mode') === 'true') {
        const refreshTokenElement = document.getElementById('refresh-token-full');
        if (refreshTokenElement && refreshTokenElement.textContent) {
            localStorage.setItem('oauth2-last-refresh-token', refreshTokenElement.textContent.trim());
            console.log('Refresh token saved for development mode');
        }
    }
}

// Function to show refresh token flow completion message
function showRefreshTokenSuccess() {
    const flowType = document.querySelector('meta[name="flow-type"]')?.content;
    if (flowType === 'refresh') {
        showNotification('🔄 Tokens renovados com sucesso! Você pode usar o novo refresh token para futuras renovações.', 'success');
        saveRefreshTokenForDev();
    }
}

// Function to copy refresh token specifically
function copyRefreshToken() {
    const refreshTokenElement = document.getElementById('refresh-token-full');
    if (refreshTokenElement) {
        copyToClipboard(refreshTokenElement.textContent, 'Refresh token copiado!');
    }
}

// Enhanced copy all tokens function for refresh flow
function copyAllTokensEnhanced() {
    const tokens = [];

    const accessToken = document.getElementById('access-token-full')?.textContent;
    const idToken = document.getElementById('id-token-full')?.textContent;
    const refreshToken = document.getElementById('refresh-token-full')?.textContent;

    if (accessToken) tokens.push(`Access Token: ${accessToken}`);
    if (idToken) tokens.push(`ID Token: ${idToken}`);
    if (refreshToken) tokens.push(`Refresh Token: ${refreshToken}`);

    if (tokens.length > 0) {
        const allTokens = tokens.join('\n\n');
        copyToClipboard(allTokens, 'Todos os tokens copiados!');
    }
}

// Initialize refresh token specific features
document.addEventListener('DOMContentLoaded', function() {
    // Show success message for refresh token flow
    showRefreshTokenSuccess();

    // Add refresh token copy button if it exists
    const refreshTokenPanel = document.getElementById('refresh-token-details');
    if (refreshTokenPanel) {
        const copyBtn = refreshTokenPanel.querySelector('.copy-code-btn');
        if (copyBtn) {
            copyBtn.addEventListener('click', copyRefreshToken);
        }
    }

    // Override copy all tokens function
    const copyAllBtn = document.querySelector('button[onclick="copyAllTokens()"]');
    if (copyAllBtn) {
        copyAllBtn.onclick = copyAllTokensEnhanced;
    }
});