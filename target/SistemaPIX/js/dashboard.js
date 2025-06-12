document.addEventListener('DOMContentLoaded', function () {
    // SELETORES ATUALIZADOS PARA CORRESPONDER AO NOVO HTML
    const greetingMessage = document.getElementById('greeting');
    const balanceValue = document.getElementById('balance');
    const logoutBtn = document.getElementById('logout-btn'); // O botão de logout agora tem um ID
    const card = document.querySelector('.card');

    function loadDashboardData() {
        // URL ATUALIZADA PARA SER RELATIVA (melhor prática)
        fetch('api/data/dashboard')
            .then(async response => {
                if (!response.ok) {
                    if (response.status === 401 || response.status === 403) {
                        // Se não estiver autorizado, redireciona para o login
                        window.location.href = 'login.html';
                        return; // Para a execução
                    }
                    const errorData = await response.json().catch(() => ({ message: 'Não foi possível ler a resposta do servidor.' }));
                    throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(apiResponse => {
                if (apiResponse.success && apiResponse.data) {
                    // MANTIDO: Sua lógica de preenchimento de dados
                    greetingMessage.textContent = `Olá, ${apiResponse.data.nomeCliente}!`;
                    // MANTIDO: Sua excelente formatação de saldo para o padrão brasileiro
                    balanceValue.textContent = `R$ ${parseFloat(apiResponse.data.saldo).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
                } else {
                    throw new Error(apiResponse.message || 'Erro ao processar dados do servidor.');
                }
            })
            .catch(error => {
                // MANTIDO: Seu excelente tratamento de erro na tela
                console.error('Falha ao carregar dados do dashboard:', error);
                if (card) {
                    card.innerHTML = `
                        <div style="padding: 20px; text-align: center;">
                            <h2>Erro Crítico</h2>
                            <p>Não foi possível carregar os dados do painel.</p>
                            <p><strong>Motivo:</strong> ${error.message}</p>
                            <button id="logout-btn-error" class="btn-secondary">Sair</button>
                        </div>`;
                    document.getElementById('logout-btn-error').addEventListener('click', handleLogout);
                }
            });
    }

    function handleLogout() {
        // URL ATUALIZADA PARA SER RELATIVA (melhor prática)
        // Assumindo que seu LogoutServlet está em /auth/logout
        fetch('auth/logout', { method: 'POST' })
            .finally(() => {
                // MANTIDO: Sua lógica de logout
                console.log('Logout solicitado. Redirecionando...');
                window.location.href = 'login.html';
            });
    }
    
    // O botão de logout agora não está em um form, então precisa de um listener
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleLogout);
    }

    loadDashboardData();
});