/**
 * =============================================================================
 * COMMON JAVASCRIPT - OAuth2 Client Demo
 * =============================================================================
 */

/**
 * Utility Functions
 */
const OAuth2Utils = {
    /**
     * Gera um state aleatório para segurança OAuth2
     */
    generateRandomState: function() {
        return 'state_' + Math.random().toString(36).substr(2, 9);
    },

    /**
     * Gera um code verifier para PKCE
     */
    generateCodeVerifier: function() {
        const array = new Uint32Array(56/2);
        window.crypto.getRandomValues(array);
        return Array.from(array, dec => ('0' + dec.toString(16)).substr(-2)).join('');
    },

    /**
     * Mostra loading state em um botão
     */
    showButtonLoading: function(buttonId, loadingText = 'Processando...') {
        const button = document.getElementById(buttonId);
        if (button) {
            button.disabled = true;
            button.setAttribute('data-original-text', button.textContent);
            button.textContent = loadingText;
        }
    },

    /**
     * Remove loading state de um botão
     */
    hideButtonLoading: function(buttonId) {
        const button = document.getElementById(buttonId);
        if (button) {
            button.disabled = false;
            const originalText = button.getAttribute('data-original-text');
            if (originalText) {
                button.textContent = originalText;
                button.removeAttribute('data-original-text');
            }
        }
    },

    /**
     * Valida URL
     */
    isValidUrl: function(string) {
        try {
            new URL(string);
            return true;
        } catch (_) {
            return false;
        }
    },

    /**
     * Copia texto para clipboard
     */
    copyToClipboard: function(text) {
        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(text);
        } else {
            // Fallback para navegadores mais antigos
            const textArea = document.createElement("textarea");
            textArea.value = text;
            document.body.appendChild(textArea);
            textArea.focus();
            textArea.select();
            try {
                document.execCommand('copy');
            } catch (err) {
                console.error('Erro ao copiar:', err);
            }
            document.body.removeChild(textArea);
        }
    },

    /**
     * Adiciona botão de copiar ao lado de códigos
     */
    addCopyButtons: function() {
        const codeValues = document.querySelectorAll('.code-value');
        codeValues.forEach(codeValue => {
            if (codeValue.textContent.trim() && !codeValue.querySelector('.copy-btn')) {
                const copyBtn = document.createElement('button');
                copyBtn.className = 'copy-btn';
                copyBtn.innerHTML = '📋';
                copyBtn.title = 'Copiar';
                copyBtn.style.cssText = `
                    position: absolute;
                    top: 10px;
                    right: 10px;
                    background: #4299e1;
                    color: white;
                    border: none;
                    border-radius: 5px;
                    padding: 5px 8px;
                    cursor: pointer;
                    font-size: 0.9em;
                `;
                
                copyBtn.addEventListener('click', () => {
                    OAuth2Utils.copyToClipboard(codeValue.textContent.trim());
                    copyBtn.innerHTML = '✅';
                    setTimeout(() => {
                        copyBtn.innerHTML = '📋';
                    }, 2000);
                });
                
                const parent = codeValue.closest('.code-display');
                if (parent) {
                    parent.style.position = 'relative';
                    parent.appendChild(copyBtn);
                }
            }
        });
    },

    /**
     * Adiciona validação em tempo real aos campos de URL
     */
    addUrlValidation: function() {
        const urlFields = document.querySelectorAll('input[type="url"]');
        urlFields.forEach(field => {
            field.addEventListener('blur', function() {
                if (this.value && !OAuth2Utils.isValidUrl(this.value)) {
                    this.style.borderColor = '#e53e3e';
                    this.style.boxShadow = '0 0 0 3px rgba(229, 62, 62, 0.1)';
                    
                    // Remove mensagem anterior se existir
                    const existingError = this.parentNode.querySelector('.url-error');
                    if (existingError) {
                        existingError.remove();
                    }
                    
                    // Adiciona mensagem de erro
                    const errorMsg = document.createElement('div');
                    errorMsg.className = 'url-error';
                    errorMsg.textContent = 'URL inválida';
                    errorMsg.style.cssText = 'color: #e53e3e; font-size: 0.9em; margin-top: 5px;';
                    this.parentNode.appendChild(errorMsg);
                } else {
                    this.style.borderColor = '#e2e8f0';
                    this.style.boxShadow = '';
                    
                    // Remove mensagem de erro
                    const existingError = this.parentNode.querySelector('.url-error');
                    if (existingError) {
                        existingError.remove();
                    }
                }
            });
        });
    }
};

/**
 * Inicialização comum
 */
document.addEventListener('DOMContentLoaded', function() {
    // Adiciona botões de copiar
    OAuth2Utils.addCopyButtons();
    
    // Adiciona validação de URL
    OAuth2Utils.addUrlValidation();
    
    // Adiciona animações suaves aos elementos
    const cards = document.querySelectorAll('.flow-card, .section, .client-option');
    cards.forEach((card, index) => {
        card.style.animationDelay = `${index * 0.1}s`;
    });
});