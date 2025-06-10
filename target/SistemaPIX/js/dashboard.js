document.addEventListener('DOMContentLoaded', function() {

    // Função para buscar os dados do dashboard no servidor
    function fetchDashboardData() {
        fetch('api/data/dashboard')
            .then(response => {
                if (response.status === 401) { // Não autorizado
                    // Se a sessão expirou ou é inválida, o filtro já deve ter redirecionado.
                    // Mas como garantia, redirecionamos aqui também.
                    window.location.href = 'login.html';
                    return;
                }
                if (!response.ok) {
                    throw new Error('Falha ao buscar dados do dashboard.');
                }
                return response.json();
            })
            .then(data => {
                if (data) {
                    // Atualiza a tela com os dados recebidos
                    document.getElementById('welcome-message').textContent = `Olá, ${data.nomeCliente}!`;
                    
                    // Formata o saldo para o formato de moeda brasileira
                    const saldoFormatado = new Intl.NumberFormat('pt-BR', {
                        style: 'currency',
                        currency: 'BRL'
                    }).format(data.saldo);

                    document.getElementById('saldo-display').textContent = saldoFormatado;
                }
            })
            .catch(error => {
                console.error('Erro:', error);
                document.getElementById('welcome-message').textContent = 'Erro ao carregar dados.';
            });
    }

    // Lógica para o botão de logout
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(event) {
            event.preventDefault(); // Impede que o link recarregue a página
            
            fetch('api/auth/logout')
                .then(response => {
                    // Após o logout bem-sucedido, redireciona para a página de login
                    window.location.href = 'login.html';
                })
                .catch(error => {
                    console.error('Erro ao fazer logout:', error);
                    alert('Não foi possível sair. Tente novamente.');
                });
        });
    }

    // Chama a função para buscar os dados assim que a página carregar
    fetchDashboardData();
});
