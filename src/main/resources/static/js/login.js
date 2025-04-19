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

    // Если сервер вернёт ошибку — это уже обрабатывается Spring Security (нужно добавить error=true в query)
});
