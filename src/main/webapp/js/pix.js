document.addEventListener('DOMContentLoaded', function() {
    const valorInput = document.getElementById('valor');
    const chaveInput = document.getElementById('chave_pix_destino');
    const tipoChaveSelect = document.getElementById('tipo_chave_destino');
    const form = document.getElementById('pix-transfer-form');
    const messageDiv = document.getElementById('message-div');

    function cpfMask(v) { return v.replace(/\D/g, '').replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d{1,2})$/, '$1-$2'); }
    function phoneMask(v) { return v.replace(/\D/g, '').replace(/^(\d{2})(\d)/g, '($1) $2').replace(/(\d{5})(\d)/, '$1-$2'); }
    function validateEmail(email) { const re = /^[a-zA-Z0-9._%+-]{3,}@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}(\.[a-zA-Z]{2,})?$/; return re.test(String(email).toLowerCase());}

    function showMessage(message, isSuccess) {
        messageDiv.textContent = message;
        messageDiv.className = isSuccess ? 'message success-message' : 'message error-message';
        messageDiv.style.display = 'block';
    }

    valorInput.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, '');
        // Limita o comprimento para evitar valores absurdos (ex: R$ 999.999.999,99)
        if (value.length > 11) {
            value = value.substring(0, 11);
        }
        value = (Number(value) / 100).toLocaleString('pt-BR', { minimumFractionDigits: 2 });
        e.target.value = (value === '0,00') ? '' : value;
    });

    function applyChaveMask() {
        const tipo = tipoChaveSelect.value;
        chaveInput.value = '';
        chaveInput.maxLength = 100;
        if (tipo === 'CPF') chaveInput.maxLength = 14;
        if (tipo === 'TELEFONE') chaveInput.maxLength = 15;
    }

    chaveInput.addEventListener('input', function() {
        const tipo = tipoChaveSelect.value;
        if (tipo === 'CPF') this.value = cpfMask(this.value);
        if (tipo === 'TELEFONE') this.value = phoneMask(this.value);
    });

    tipoChaveSelect.addEventListener('change', applyChaveMask);

    form.addEventListener('submit', function(event) {
        event.preventDefault();
        messageDiv.style.display = 'none';

        if (tipoChaveSelect.value === 'EMAIL' && !validateEmail(chaveInput.value)) {
            return showMessage('Por favor, insira um e-mail de destino válido.', false);
        }

        const valorNumerico = parseFloat(valorInput.value.replace(/\./g, '').replace(',', '.'));
        if (isNaN(valorNumerico) || valorNumerico <= 0) {
            return showMessage('Por favor, insira um valor válido para a transferência.', false);
        }
        
        // Lógica de fetch para a transferência viria aqui...
        // Exemplo:
        // fetch('/SistemaPIX/api/pix/transfer', { ... })
        showMessage('Funcionalidade de transferência ainda em implementação.', true);
    });

    applyChaveMask();
});
