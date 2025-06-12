document.addEventListener('DOMContentLoaded', () => {

    // --- Seletores de Elementos ---
    const form = document.getElementById('add-key-form');
    const tipoChaveSelect = document.getElementById('tipo_chave');
    const valorChaveInput = document.getElementById('valor_chave');
    const valorChaveGroup = document.getElementById('valor-chave-group');
    const keysListContainer = document.getElementById('keys-list');
    const messageDiv = document.getElementById('message-div');

    // Modais
    const deleteModal = document.getElementById('delete-confirm-modal');
    const confirmDeleteBtn = document.getElementById('confirm-delete-btn');
    const cancelDeleteBtn = document.getElementById('cancel-delete-btn');
    const editModal = document.getElementById('edit-key-modal');
    const editModalTitle = document.getElementById('edit-modal-title');
    const editValorChaveInput = document.getElementById('edit_valor_chave');
    const editChaveIdInput = document.getElementById('edit_chave_id');
    const saveEditBtn = document.getElementById('save-edit-btn');
    const cancelEditBtn = document.getElementById('cancel-edit-btn');
    
    let userInfo = {};
    let userChaves = [];
    let keyIdParaDeletar = null;
    let tipoChaveParaEditar = '';

    // --- Funções de Validação e Formatação ---

    const formatarCPF = (cpf) => {
        return cpf.replace(/\D/g, '')
            .replace(/(\d{3})(\d)/, '$1.$2')
            .replace(/(\d{3})(\d)/, '$1.$2')
            .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    };

    const formatarTelefone = (tel) => {
        return tel.replace(/\D/g, '')
            .replace(/^(\d{2})(\d)/g, '($1) $2')
            .replace(/(\d{5})(\d)/, '$1-$2');
    };

    const validarEmail = (email) => {
        const re = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
        return re.test(String(email).toLowerCase());
    };

    const aplicarMascara = (inputElement, tipoChave) => {
        if (tipoChave === 'CPF') {
            inputElement.value = formatarCPF(inputElement.value);
            inputElement.maxLength = 14;
        } else if (tipoChave === 'TELEFONE') {
            inputElement.value = formatarTelefone(inputElement.value);
            inputElement.maxLength = 15;
        } else {
            inputElement.removeAttribute('maxLength');
        }
    };
    
    // --- Funções de Lógica Principal ---

    const showMessage = (message, isError = false) => {
        messageDiv.textContent = message;
        messageDiv.className = isError ? 'message error-message' : 'message success-message';
        messageDiv.style.display = 'block';
        setTimeout(() => { messageDiv.style.display = 'none'; }, 4000);
    };
    
    const updateFormOptions = () => {
        const tiposCadastrados = userChaves.map(chave => chave.tipoChave);
        Array.from(tipoChaveSelect.options).forEach(option => {
            option.disabled = tiposCadastrados.includes(option.value);
        });
        // Força a re-seleção para uma opção válida se a atual for desabilitada
        if (tipoChaveSelect.options[tipoChaveSelect.selectedIndex].disabled) {
            tipoChaveSelect.selectedIndex = Array.from(tipoChaveSelect.options).findIndex(opt => !opt.disabled);
        }
        updateFormFields();
    };

    const fetchKeysAndRender = async () => {
        try {
            const response = await fetch('api/data/chaves'); 
            const result = await response.json();

            keysListContainer.innerHTML = '';
            userChaves = []; // Limpa antes de preencher

            if (result.success && result.data.length > 0) {
                userChaves = result.data;
                userChaves.forEach(key => {
                    // REGRA: CPF e Aleatória não são editáveis
                    const isEditable = key.tipoChave !== 'ALEATORIA' && key.tipoChave !== 'CPF';
                    
                    const keyElement = document.createElement('div');
                    keyElement.className = 'chave-item';
                    keyElement.innerHTML = `
                        <div class="chave-item-info">
                            <strong>${key.tipoChave}:</strong>
                            <span>${key.valorChave}</span>
                        </div>
                        <div class="chave-item-actions">
                            ${isEditable ? `<button class="action-button edit" title="Editar" data-id="${key.id}" data-valor="${key.valorChave}" data-tipo="${key.tipoChave}">✏️</button>` : '<div style="width:36px; height: 36px;"></div>'}
                            <button class="action-button delete" title="Excluir" data-id="${key.id}">🗑️</button>
                        </div>
                    `;
                    keysListContainer.appendChild(keyElement);
                });
            } else {
                keysListContainer.innerHTML = '<p>Nenhuma chave cadastrada.</p>';
            }
            updateFormOptions(); // ATUALIZA O FORMULÁRIO APÓS RENDERIZAR
        } catch (error) {
            showMessage('Erro de conexão ao carregar chaves.', true);
        }
    };

    const fetchUserInfo = async () => {
        try {
            const response = await fetch('api/data/user-info');
            const result = await response.json();
            if (result.success) {
                userInfo = result.data;
                updateFormFields();
            }
        } catch (error) { 
            showMessage('Erro ao buscar info do usuário', true);
        }
    };
    
    const updateFormFields = () => {
        const tipo = tipoChaveSelect.value;
        valorChaveGroup.style.display = 'block';
        valorChaveInput.readOnly = false;
        valorChaveInput.value = ''; // Limpa o campo por padrão

        if(tipo === 'CPF') {
            valorChaveInput.value = userInfo.cpf || '';
            valorChaveInput.readOnly = true; // REGRA: CPF é somente leitura
        } else if (tipo === 'ALEATORIA') {
            valorChaveGroup.style.display = 'none';
        }
        aplicarMascara(valorChaveInput, tipo);
    };
    
    // --- Event Listeners ---

    valorChaveInput.addEventListener('input', () => aplicarMascara(valorChaveInput, tipoChaveSelect.value));
    editValorChaveInput.addEventListener('input', () => aplicarMascara(editValorChaveInput, tipoChaveParaEditar));
    tipoChaveSelect.addEventListener('change', updateFormFields);

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const tipoChave = tipoChaveSelect.value;
        let valorChave = valorChaveInput.value;

        // Validações antes do envio
        if (tipoChave === 'EMAIL' && !validarEmail(valorChave)) {
            return showMessage('Formato de e-mail inválido.', true);
        }
        if ((tipoChave === 'CPF' && valorChave.length !== 14) || (tipoChave === 'TELEFONE' && valorChave.length !== 15)) {
            return showMessage('Preencha o campo corretamente.', true);
        }

        if(tipoChave === 'ALEATORIA') {
            valorChave = `aleatoria-${Date.now()}`;
        }

        try {
            const response = await fetch('api/data/chaves', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ tipoChave, valorChave })
            });
            const result = await response.json();
            if(result.success) {
                showMessage('Chave adicionada com sucesso!');
                form.reset();
                fetchKeysAndRender();
            } else {
                showMessage(result.message, true);
            }
        } catch (error) {
            showMessage('Erro de conexão ao adicionar chave.', true);
        }
    });

    keysListContainer.addEventListener('click', (e) => {
        const button = e.target.closest('.action-button');
        if (!button) return;

        const keyId = button.dataset.id;
        if (button.classList.contains('delete')) {
            keyIdParaDeletar = keyId;
            deleteModal.classList.add('visible');
        } else if (button.classList.contains('edit')) {
            tipoChaveParaEditar = button.dataset.tipo;
            editModalTitle.textContent = `Editar Chave ${tipoChaveParaEditar}`;
            editValorChaveInput.value = button.dataset.valor;
            editChaveIdInput.value = keyId;
            editModal.classList.add('visible');
            aplicarMascara(editValorChaveInput, tipoChaveParaEditar);
        }
    });

    confirmDeleteBtn.addEventListener('click', async () => {
        try {
            const response = await fetch(`api/data/chaves?id=${keyIdParaDeletar}`, { method: 'DELETE' });
            const result = await response.json();
            if(result.success) {
                showMessage('Chave removida com sucesso!');
                fetchKeysAndRender();
            } else { showMessage(result.message, true); }
        } catch (error) { showMessage('Erro de conexão ao remover chave.', true);
        } finally { deleteModal.classList.remove('visible'); }
    });

    saveEditBtn.addEventListener('click', async () => {
        const id = editChaveIdInput.value;
        const valorChave = editValorChaveInput.value;
        
        // Validações antes do envio da edição
        if (tipoChaveParaEditar === 'EMAIL' && !validarEmail(valorChave)) {
            return showMessage('Formato de e-mail inválido.', true);
        }
        if (tipoChaveParaEditar === 'TELEFONE' && valorChave.length !== 15) {
            return showMessage('Preencha o telefone corretamente.', true);
        }

        try {
            const response = await fetch('api/data/chaves', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id: Number(id), valorChave })
            });
            const result = await response.json();
            if(result.success) {
                showMessage('Chave atualizada com sucesso!');
                fetchKeysAndRender();
            } else { showMessage(result.message, true); }
        } catch(error) { showMessage('Erro de conexão ao salvar chave.', true);
        } finally { editModal.classList.remove('visible'); }
    });
    
    // Listeners para fechar modais
    cancelDeleteBtn.addEventListener('click', () => deleteModal.classList.remove('visible'));
    cancelEditBtn.addEventListener('click', () => editModal.classList.remove('visible'));

    // --- Inicialização da Página ---
    fetchUserInfo().then(() => {
        fetchKeysAndRender();
    });
});