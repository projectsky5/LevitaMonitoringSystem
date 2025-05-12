export async function getCsrfToken() {
    const res = await fetch('/csrf', { credentials: 'same-origin' });
    const data = await res.json();
    return data.token;
}

export async function csrfFetch(url, options = {}) {
    const token = await getCsrfToken();
    return fetch(url, {
        credentials: 'include',
        ...options,
        headers: {
            ...(options.headers || {}),
            'X-XSRF-TOKEN': token
        }
    });
}