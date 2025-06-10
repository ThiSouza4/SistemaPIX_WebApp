document.addEventListener('DOMContentLoaded', function () {
    const pixForm = document.getElementById('pixForm');
    const mensagemArea = document.getElementById('mensagemArea');
    
    const confirmacaoArea = document.getElementById('confirmacaoArea');
    const mensagemConfirmacaoTexto = document.getElementById('mensagemConfirmacaoTexto');
    const btnConfirmarPixSim = document.getElementById('btnConfirmarPixSim');
    const btnCancelarPixNao = document.getElementById('btnCancelarPixNao');
    const btnRealizarTransferencia = document.getElementById('btnRealizarTransferencia');

    let dadosTransferenciaPendente = null; // Para guardar dados se a confirmação for necessária

    pixForm.addEventListener('submit', async function (event) {
        event.preventDefault(); // Impede o envio padrão do formulário
        // Não é uma confirmação direta do botão de submissão inicial
        await processarEnvioTransferencia(false); 
    });

    btnConfirmarPixSim.addEventListener('click', async function() {
        if (dadosTransferenciaPendente) {
            // O usuário clicou "Sim" na caixa de diálogo de confirmação.
            await processarEnvioTransferencia(true); 
        }
        ocultarDialogoConfirmacao();
    });

    btnCancelarPixNao.addEventListener('click', function() {
        exibirMensagemNaTela('Transferência cancelada pelo usuário.', 'aviso');
        ocultarDialogoConfirmacao();
        habilitarFormulario();
    });

    async function processarEnvioTransferencia(confirmadoPeloUsuario) {
        desabilitarFormulario(); // Desabilita o formulário para evitar envios múltiplos
        limparMensagemDaTela();
        ocultarDialogoConfirmacao();

        let requestData;

        if (confirmadoPeloUsuario && dadosTransferenciaPendente) {
            // Se o usuário confirmou, usamos os dados que estavam pendentes e marcamos como confirmado.
            requestData = { ...dadosTransferenciaPendente, confirmado: true };
        } else {
            // Coleta os dados do formulário para uma nova tentativa ou a primeira tentativa.
            const idContaOrigem = parseInt(document.getElementById('idContaOrigem').value);
            const chavePixDestino = document.getElementById('chavePixDestino').value.trim();
            const valor = parseFloat(document.getElementById('valor').value);

            // Validação básica no front-end
            if (isNaN(idContaOrigem) || idContaOrigem <=0 || !chavePixDestino || isNaN(valor) || valor <= 0) {
                exibirMensagemNaTela('Por favor, preencha todos os campos corretamente (ID da Conta, Chave PIX e Valor maior que zero).', 'erro');
                habilitarFormulario();
                return;
            }
            requestData = { idContaOrigem, chavePixDestino, valor, confirmado: false };
            // Guarda os dados atuais caso uma confirmação seja necessária posteriormente.
            dadosTransferenciaPendente = { idContaOrigem, chavePixDestino, valor }; 
        }

        try {
            // Determina a URL base da aplicação.
            // Se estiver rodando localmente como 'http://localhost:8080/SistemaPIX_WebApp/', a baseURL será '/SistemaPIX_WebApp'
            // Se estiver na raiz (ex: http://localhost:8080/), a baseURL será ''
            const pathArray = window.location.pathname.split('/');
            const appName = pathArray.length > 1 && pathArray[1] !== "index.html" ? "/" + pathArray[1] : ""; 
            const apiURL = `${window.location.origin}${appName}/api/pix/transfer`;
            
            console.log("Enviando para API:", apiURL, "com dados:", requestData);

            const response = await fetch(apiURL, { 
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json' // Indica que esperamos uma resposta JSON
                },
                body: JSON.stringify(requestData),
            });

            const result = await response.json(); // Tenta parsear a resposta como JSON

            console.log("Resposta da API:", response.status, result);

            if (result.status === 'CONFIRMATION_REQUIRED') {
                // O back-end indicou que a confirmação do usuário é necessária.
                mostrarDialogoConfirmacao(result.message);
                // Não habilita o formulário ainda, espera a decisão do usuário na caixa de diálogo.
            } else if (result.status === 'SUCCESS' || result.status === 'EM_ANALISE') {
                // Transferência bem-sucedida ou em análise.
                exibirMensagemNaTela(result.message, result.status === 'SUCCESS' ? 'sucesso' : 'aviso');
                pixForm.reset(); // Limpa o formulário
                dadosTransferenciaPendente = null; // Limpa os dados pendentes
                habilitarFormulario();
            } else { 
                // Outros erros lógicos retornados pelo back-end com status HTTP OK, mas com erro na resposta.
                exibirMensagemNaTela(result.message || 'Erro desconhecido ao processar transferência.', 'erro');
                habilitarFormulario();
            }
        } catch (error) {
            // Erros de rede ou falha ao parsear JSON (se response.json() falhar)
            console.error('Erro na requisição fetch:', error);
            exibirMensagemNaTela('Não foi possível conectar ao servidor ou ocorreu um erro na resposta. Verifique o console para detalhes.', 'erro');
            habilitarFormulario();
        }
    }

    function mostrarDialogoConfirmacao(mensagem) {
        mensagemConfirmacaoTexto.textContent = mensagem;
        confirmacaoArea.style.display = 'block';
        mensagemArea.style.display = 'none'; // Oculta outras mensagens
        // O formulário principal permanece desabilitado enquanto o diálogo está visível.
    }

    function ocultarDialogoConfirmacao() {
        confirmacaoArea.style.display = 'none';
        if(!dadosTransferenciaPendente || !document.getElementById("chavePixDestino").value){ // Se não há mais transferencia pendente OU se o form foi resetado
             dadosTransferenciaPendente = null; // Limpa os dados pendentes se o diálogo for fechado
        }
    }

    function exibirMensagemNaTela(msg, tipo) {
        mensagemArea.textContent = msg;
        mensagemArea.className = 'mensagem ' + tipo; // Adiciona a classe do tipo (sucesso, erro, aviso)
        mensagemArea.style.display = 'block'; // Torna a área de mensagem visível
    }

    function limparMensagemDaTela() {
        mensagemArea.textContent = '';
        mensagemArea.style.display = 'none'; // Oculta a área de mensagem
    }

    function desabilitarFormulario() {
        btnRealizarTransferencia.disabled = true;
        btnRealizarTransferencia.textContent = 'Processando...';
        // Pode desabilitar outros campos se necessário
        document.getElementById('idContaOrigem').disabled = true;
        document.getElementById('chavePixDestino').disabled = true;
        document.getElementById('valor').disabled = true;
    }

    function habilitarFormulario() {
        btnRealizarTransferencia.disabled = false;
        btnRealizarTransferencia.textContent = 'Realizar Transferência';
        document.getElementById('idContaOrigem').disabled = false;
        document.getElementById('chavePixDestino').disabled = false;
        document.getElementById('valor').disabled = false;
    }
});
