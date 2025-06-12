document.addEventListener('DOMContentLoaded', function () {
    const themeSwitcher = document.getElementById('theme-switcher');
    const currentTheme = localStorage.getItem('theme');

    // Aplica o tema salvo ao carregar a página
    if (currentTheme) {
        document.body.classList.add(currentTheme);
        if (currentTheme === 'light-theme') {
            if (themeSwitcher) themeSwitcher.textContent = '🌙';
        } else {
            if (themeSwitcher) themeSwitcher.textContent = '☀️';
        }
    }

    // Adiciona o evento de clique no botão (se ele existir na página)
    if (themeSwitcher) {
        themeSwitcher.addEventListener('click', function () {
            document.body.classList.toggle('light-theme');
            let theme = 'dark-theme'; // Padrão
            if (document.body.classList.contains('light-theme')) {
                theme = 'light-theme';
                themeSwitcher.textContent = '🌙';
            } else {
                themeSwitcher.textContent = '☀️';
            }
            localStorage.setItem('theme', theme);
        });
    }
});