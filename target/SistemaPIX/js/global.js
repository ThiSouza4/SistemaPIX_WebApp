// Conteúdo COMPLETO para o NOVO arquivo: js/global.js

document.addEventListener('DOMContentLoaded', () => {
    // 1. Aplica o tema salvo no localStorage assim que a página carrega
    const savedTheme = localStorage.getItem('theme') || '';
    if (savedTheme) {
        document.body.classList.add(savedTheme);
    }

    // 2. Cria o botão de troca de tema dinamicamente
    const themeToggleButton = document.createElement('div');
    themeToggleButton.className = 'theme-toggle-float';
    themeToggleButton.id = 'theme-toggle';
    themeToggleButton.innerHTML = savedTheme === 'light-theme' ? '&#127769;' : '&#9728;&#65039;';
    document.body.appendChild(themeToggleButton);

    // 3. Adiciona a lógica de clique ao botão
    themeToggleButton.addEventListener('click', () => {
        document.body.classList.toggle('light-theme');
        let theme = '';
        if (document.body.classList.contains('light-theme')) {
            theme = 'light-theme';
            themeToggleButton.innerHTML = '&#127769;'; // Ícone de lua
        } else {
            themeToggleButton.innerHTML = '&#9728;&#65039;'; // Ícone de sol
        }
        localStorage.setItem('theme', theme); // Salva a escolha
    });
});
