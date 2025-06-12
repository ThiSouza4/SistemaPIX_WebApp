// Funções de máscara e validação (sem alterações)
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
            
            fetch('/SistemaPIX/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: email, senha: senha }),
            })
            .then(async response => {
                const data = await response.json(); // Sempre tenta ler o JSON
                if (response.ok) {
                    window.location.href = 'dashboard.html';
                } else {
                    throw new Error(data.message || 'Erro desconhecido.');
                }
            })
            .catch(error => {
                messageDiv.textContent = error.message;
                messageDiv.className = 'message error-message';
                messageDiv.style.display = 'block';
            });
        });
    }

    // --- LÓGICA DE CADASTRO ---
    const registerForm = document.getElementById('cadastro-form');
    if (registerForm) {
        // Código das máscaras (sem alterações)...
        const cpfInput = document.getElementById('cpf');
        const telefoneInput = document.getElementById('telefone');
        cpfInput.maxLength = 14;
        telefoneInput.maxLength = 15;
        cpfInput.addEventListener('input', () => { cpfInput.value = applyMask(cpfInput.value, cpfMask, 14); });
        telefoneInput.addEventListener('input', () => { telefoneInput.value = applyMask(telefoneInput.value, phoneMask, 15); });

        registerForm.addEventListener('submit', function (event) {
            event.preventDefault();
            const messageDiv = document.getElementById('register-message');
            const cliente = { 
                nomeCompleto: document.getElementById('nome').value, 
                cpf: cpfInput.value, 
                telefone: telefoneInput.value, 
                email: document.getElementById('email').value, 
                senha: document.getElementById('senha').value 
            };
            
            fetch('/SistemaPIX/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(cliente),
            })
            .then(async response => {
                const data = await response.json(); // Sempre tenta ler o JSON
                if (response.ok) {
                    messageDiv.textContent = data.message;
                    messageDiv.className = 'message success-message';
                    messageDiv.style.display = 'block';
                    setTimeout(() => { window.location.href = 'login.html'; }, 2000);
                } else {
                    throw new Error(data.message || 'Erro ao registrar.');
                }
            })
            .catch(error => {
                messageDiv.textContent = error.message;
                messageDiv.className = 'message error-message';
                messageDiv.style.display = 'block';
            });
        });
    }
});