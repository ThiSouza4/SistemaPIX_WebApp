// Conteúdo COMPLETO para: js/auth.js

// --- FUNÇÕES DE VALIDAÇÃO E MÁSCARA ---
function validateEmail(email) {
    const re = /^[a-zA-Z0-9._%+-]{3,}@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}(\.[a-zA-Z]{2,})?$/;
    return re.test(String(email).toLowerCase());
}
function applyMask(value, maskFunction, maxLength) {
    const maskedValue = maskFunction(value);
    return maskedValue.length > maxLength ? maskedValue.substring(0, maxLength) : maskedValue;
}
function cpfMask(v) { return v.replace(/\D/g, '').replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d{1,2})$/, '$1-$2'); }
function phoneMask(v) { return v.replace(/\D/g, '').replace(/^(\d{2})(\d)/g, '($1) $2').replace(/(\d{5})(\d)/, '$1-$2'); }

document.addEventListener('DOMContentLoaded', function () {
    // --- LÓGICA DE LOGIN ---
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', function (event) {
            event.preventDefault();
            const email = document.getElementById('email').value;
            const senha = document.getElementById('senha').value;
            const messageDiv = document.getElementById('login-message');
            fetch('api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: email, senha: senha }),
            })
            .then(response => {
                if (response.ok) { window.location.href = 'dashboard.html'; } 
                else { return response.json().then(data => { throw new Error(data.message || 'Erro desconhecido.'); }); }
            })
            .catch(error => {
                messageDiv.textContent = error.message;
                messageDiv.className = 'message error-message';
                messageDiv.style.display = 'block';
            });
        });
    }

    // --- LÓGICA DE CADASTRO ---
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        const cpfInput = document.getElementById('cpf');
        const telefoneInput = document.getElementById('telefone');
        cpfInput.maxLength = 14;
        telefoneInput.maxLength = 15;
        cpfInput.addEventListener('input', () => { cpfInput.value = applyMask(cpfInput.value, cpfMask, 14); });
        telefoneInput.addEventListener('input', () => { telefoneInput.value = applyMask(telefoneInput.value, phoneMask, 15); });

        registerForm.addEventListener('submit', function (event) {
            event.preventDefault();
            const emailInput = document.getElementById('email');
            const messageDiv = document.getElementById('register-message');
            if (!validateEmail(emailInput.value)) {
                messageDiv.textContent = 'Por favor, insira um e-mail válido.';
                messageDiv.className = 'message error-message';
                messageDiv.style.display = 'block';
                return;
            }
            const cliente = { nome: document.getElementById('nome').value, cpf: cpfInput.value, telefone: telefoneInput.value, email: emailInput.value, senhaHash: document.getElementById('senha').value };
            fetch('api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(cliente),
            })
            .then(response => response.json().then(data => ({ status: response.status, body: data })))
            .then(obj => {
                messageDiv.textContent = obj.body.message;
                messageDiv.className = (obj.status >= 200 && obj.status < 300) ? 'message success-message' : 'message error-message';
                messageDiv.style.display = 'block';
                if (obj.status >= 200 && obj.status < 300) {
                    setTimeout(() => { window.location.href = 'login.html'; }, 2000);
                }
            })
            .catch(error => {
                messageDiv.textContent = 'Erro de conexão. Tente novamente.';
                messageDiv.className = 'message error-message';
                messageDiv.style.display = 'block';
            });
        });
    }

    // --- LÓGICA DO SELETOR DE TEMA ---
    // Uma cópia simplificada para garantir que o tema seja aplicado ao carregar
    const currentTheme = localStorage.getItem('theme');
    if (currentTheme === 'light-theme') {
        document.body.classList.add('light-theme');
    }
});
