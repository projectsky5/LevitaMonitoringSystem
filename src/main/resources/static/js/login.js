document.getElementById('loginForm').addEventListener('submit', function (e) {
    const username = document.getElementById('username');
    const password = document.getElementById('password');
    const errorMessage = document.getElementById('errorMessage');

    let hasError = false;

    [username, password].forEach(input => {
        input.classList.remove('is-invalid');
        if (input.value.trim() === '') {
            input.classList.add('is-invalid');
            hasError = true;
        }
    });

    if (hasError) {
        e.preventDefault();
        errorMessage.textContent = 'Пожалуйста, заполните все поля.';
        errorMessage.classList.remove('d-none');
        return;
    }
});

document.addEventListener("DOMContentLoaded", () => {
    const urlParams = new URLSearchParams(window.location.search);
    const hasError = urlParams.get("error");

    const errorBox = document.getElementById("errorMessage");
    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");
    const loginForm = document.getElementById("loginForm");

    // При ошибке авторизации
    if (hasError) {
        showError("Неверные учетные данные пользователя");
    }

    // При фокусе на любом поле — убираем ошибку
    [usernameInput, passwordInput].forEach(input => {
        input.addEventListener("focus", () => {
            hideError();
        });
    });

    // Обработка сабмита формы (валидация полей)
    loginForm.addEventListener("submit", (e) => {
        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();

        if (!username || !password) {
            e.preventDefault(); // Останавливаем отправку формы
            showError("Пожалуйста, заполните все поля.");
        }
    });

    function showError(message) {
        errorBox.textContent = message;
        errorBox.classList.add("visible");
        errorBox.classList.remove("invisible");
        usernameInput.classList.add("is-invalid");
        passwordInput.classList.add("is-invalid");
    }

    if (hasError) {
        showError("Неверные учетные данные пользователя");

        // Удаляем параметр `?error=true` из URL после показа ошибки
        const cleanUrl = window.location.origin + window.location.pathname;
        window.history.replaceState({}, document.title, cleanUrl);
    }

    function hideError() {
        errorBox.textContent = "";
        errorBox.classList.remove("visible");
        errorBox.classList.add("invisible");
        usernameInput.classList.remove("is-invalid");
        passwordInput.classList.remove("is-invalid");
    }
});