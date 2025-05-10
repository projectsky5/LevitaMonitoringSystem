import { csrfFetch } from './csrf.js';

document.addEventListener('DOMContentLoaded', () => {
    const form         = document.getElementById('loginForm');
    const usernameIn   = document.getElementById('username');
    const passwordIn   = document.getElementById('password');
    const errorMessage = document.getElementById('errorMessage');

    function showError(msg) {
        errorMessage.textContent = msg;
        errorMessage.classList.add('visible');
        errorMessage.classList.remove('invisible', 'd-none');
        usernameIn.classList.add('is-invalid');
        passwordIn.classList.add('is-invalid');
    }

    function hideError() {
        errorMessage.textContent = '';
        errorMessage.classList.add('invisible');
        errorMessage.classList.remove('visible');
        usernameIn.classList.remove('is-invalid');
        passwordIn.classList.remove('is-invalid');
    }

    form.addEventListener('submit', async e => {
        e.preventDefault();
        hideError();

        // простая валидация полей
        if (!usernameIn.value.trim() || !passwordIn.value.trim()) {
            return showError('Пожалуйста, заполните все поля.');
        }

        // собираем форму
        const data = new URLSearchParams({
            username: usernameIn.value,
            password: passwordIn.value
        });

        try {
            const res = await csrfFetch('/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: data
            });
            if (res.redirected) {
                window.location.href = res.url;
            } else {
                showError('Неверные учетные данные пользователя');
            }
        } catch {
            showError('Ошибка авторизации. Попробуйте позже.');
        }
    });

    // если в URL есть ?error=true
    if (new URLSearchParams(window.location.search).has('error')) {
        showError('Неверные учетные данные пользователя');
        history.replaceState(null, '', window.location.pathname);
    }

    // при фокусе на полях — сбрасываем ошибку
    [usernameIn, passwordIn].forEach(i => i.addEventListener('focus', hideError));
});