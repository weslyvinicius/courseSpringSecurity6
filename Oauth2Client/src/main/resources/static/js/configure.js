/**
 * =============================================================================
 * CONFIGURE PAGE JAVASCRIPT - OAuth2 Client Demo
 * =============================================================================
 */

document.addEventListener('DOMContentLoaded', function() {
    // Gerar state aleatório ao carregar a página
    const stateField = document.getElementById('state');
    if (stateField && !stateField.value) {
        stateField.value = OAuth2Utils.generateRandomState();
    }

    // Atualizar formulário quando cliente for selecionado
    const clientRadios = document.querySelectorAll('input[name="selectedClient"]');
    clientRadios.forEach(radio => {
        radio.addEventListener('change', function() {
            if (this.checked) {
                updateFormWithClientData(this);
            }
        });
    });

    // Validação do formulário antes do submit
    const authorizeForm = document.getElementById('authorizeForm');
    if (authorizeForm) {
        authorizeForm.addEventListener('submit', function(e) {
            if (!validateForm()) {
                e.preventDefault();
                return false;
            }
            
            // Mostra loading
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                OAuth2Utils.showButtonLoading(submitBtn.id || 'submitBtn', '🚀 Redirecionando...');
            }
        });
    }
});

/**
 * Atualiza o formulário com os dados do cliente selecionado
 */
function updateFormWithClientData(radioElement) {
    const formFields = {
        'clientName': radioElement.dataset.name || '',
        'clientId': radioElement.dataset.clientId || '',
        'clientSecret': radioElement.dataset.clientSecret || '',
        'redirectUri': radioElement.dataset.redirectUri || '',
        'authorizationEndpoint': radioElement.dataset.authEndpoint || '',
        'tokenEndpoint': radioElement.dataset.tokenEndpoint || '',
        'responseType': radioElement.dataset.responseType || '',
        'grantType': radioElement.dataset.grantType || '',
        'scope': radioElement.dataset.scopes || ''
    };

    // Atualiza os campos do formulário
    Object.keys(formFields).forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (field) {
            field.value = formFields[fieldId];
            
            // Adiciona efeito visual de atualização
            field.style.backgroundColor = '#e6fffa';
            setTimeout(() => {
                field.style.backgroundColor = '';
            }, 500);
        }
    });

    // Mostra notificação de atualização
    showClientUpdateNotification(radioElement.dataset.name);
}

/**
 * Valida o formulário antes do submit
 */
function validateForm() {
    const requiredFields = [
        'clientId', 
        'redirectUri', 
        'authorizationEndpoint', 
        'responseType'
    ];

    let isValid = true;
    const errors = [];

    requiredFields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (field && !field.value.trim()) {
            errors.push(`${field.labels[0]?.textContent || fieldId} é obrigatório`);
            field.style.borderColor = '#e53e3e';
            isValid = false;
        } else if (field) {
            field.style.borderColor = '#e2e8f0';
        }
    });

    // Validação específica de URLs
    const urlFields = ['redirectUri', 'authorizationEndpoint', 'tokenEndpoint'];
    urlFields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (field && field.value && !OAuth2Utils.isValidUrl(field.value)) {
            errors.push(`${field.labels[0]?.textContent || fieldId} deve ser uma URL válida`);
            field.style.borderColor = '#e53e3e';
            isValid = false;
        }
    });

    if (!isValid) {
        showValidationErrors(errors);
    }

    return isValid;
}

/**
 * Mostra erros de validação
 */
function showValidationErrors(errors) {
    // Remove notificações anteriores
    const existingNotification = document.querySelector('.validation-notification');
    if (existingNotification) {
        existingNotification.remove();
    }

    // Cria nova notificação
    const notification = document.createElement('div');
    notification.className = 'validation-notification';
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: linear-gradient(135deg, #fed7d7, #feb2b2);
        border: 2px solid #e53e3e;
        border-radius: 10px;
        padding: 20px;
        max-width: 400px;
        z-index: 1000;
        animation: slideInRight 0.3s ease-out;
    `;

    const title = document.createElement('h4');
    title.textContent = '❌ Erro de Validação';
    title.style.cssText = 'color: #742a2a; margin-bottom: 10px;';

    const list = document.createElement('ul');
    list.style.cssText = 'color: #742a2a; padding-left: 20px;';
    
    errors.forEach(error => {
        const item = document.createElement('li');
        item.textContent = error;
        list.appendChild(item);
    });

    notification.appendChild(title);
    notification.appendChild(list);
    document.body.appendChild(notification);

    // Remove após 5 segundos
    setTimeout(() => {
        if (notification.parentNode) {
            notification.style.animation = 'slideOutRight 0.3s ease-in';
            setTimeout(() => {
                notification.remove();
            }, 300);
        }
    }, 5000);

    // Adiciona CSS para as animações se não existir
    if (!document.querySelector('#validation-animations')) {
        const style = document.createElement('style');
        style.id = 'validation-animations';
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
    }
}

/**
 * Mostra notificação de cliente atualizado
 */
function showClientUpdateNotification(clientName) {
    // Remove notificação anterior se existir
    const existing = document.querySelector('.client-update-notification');
    if (existing) {
        existing.remove();
    }

    const notification = document.createElement('div');
    notification.className = 'client-update-notification';
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: linear-gradient(135deg, #e6fffa, #b2f5ea);
        border: 2px solid #38b2ac;
        border-radius: 10px;
        padding: 15px 20px;
        z-index: 1000;
        animation: slideInRight 0.3s ease-out;
        box-shadow: 0 10px 25px rgba(56, 178, 172, 0.2);
    `;

    notification.innerHTML = `
        <div style="color: #234e52; font-weight: 600;">
            ✅ Cliente "${clientName}" selecionado
        </div>
        <div style="color: #234e52; font-size: 0.9em; margin-top: 5px;">
            Formulário atualizado automaticamente
        </div>
    `;

    document.body.appendChild(notification);

    // Remove após 3 segundos
    setTimeout(() => {
        if (notification.parentNode) {
            notification.style.animation = 'slideOutRight 0.3s ease-in';
            setTimeout(() => {
                notification.remove();
            }, 300);
        }
    }, 3000);
}