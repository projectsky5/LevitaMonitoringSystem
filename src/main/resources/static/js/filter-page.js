import { csrfFetch } from './csrf.js';

document.addEventListener('DOMContentLoaded', function () {
    const sortTypeBtn   = document.querySelector('.btn-filter-type');
    const sortToggleBtn = document.getElementById('sortToggleBtn');
    const sortTypeMenu  = document.getElementById('sortTypeMenu');
    const sortIcon      = sortToggleBtn.querySelector('img');

    let currentSortType  = null;
    let currentSortOrder = null;

    // init
    sortIcon.src = '/assets/filter/svg/sort-off.svg';
    sortIcon.style.width  = '28px';
    sortIcon.style.height = '28px';
    sortTypeBtn.textContent = 'Выберите';

    function loadAdmins(primarySort = null, primaryOrder = null, secondarySort = null, secondaryOrder = null) {
        let url    = '/api/admins';
        const qs  = new URLSearchParams();

        if (primarySort && primaryOrder) {
            url = '/api/admins/sorted';
            qs.append('primarySort', primarySort);
            qs.append('primaryOrder', primaryOrder);
        }

        if (secondarySort && secondaryOrder) {
            qs.append('secondarySort', secondarySort);
            qs.append('secondaryOrder', secondaryOrder);
        }

        if (qs.toString()) {
            url += `?${qs.toString()}`;
        }

        csrfFetch(url)                      // <-- здесь
            .then(response => {
                if (!response.ok) {
                    throw new Error('Ошибка загрузки админов');
                }
                return response.json();
            })
            .then(data => renderAdmins(data))
            .catch(error => console.error('Ошибка:', error));
    }

    function renderAdmins(admins) {
        const listContainer = document.querySelector('.admin-list');
        listContainer.innerHTML = '';

        if (admins.length === 0) {
            listContainer.innerHTML = '<li class="list-group-item">Нет администраторов</li>';
            return;
        }

        admins.forEach(admin => {
            const li = document.createElement('li');
            li.className = 'list-group-item';
            li.innerHTML = `<span class="admin-link" data-user-id="${admin.id}">${admin.nameWithLocation}</span>`;
            listContainer.appendChild(li);
        });

        document.querySelectorAll('.admin-link').forEach(link => {
            link.addEventListener('click', () => {
                const userId = link.getAttribute('data-user-id');
                window.location.href = `/dashboard/${userId}`;
            });
        });
    }

    // цикличное переключение order
    sortToggleBtn.addEventListener('click', () => {
        if (!currentSortType) return;

        if (currentSortOrder === null) {
            currentSortOrder = 'asc';
        } else if (currentSortOrder === 'asc') {
            currentSortOrder = 'desc';
        } else {
            currentSortOrder = null;
        }

        updateSortIcon();

        if (currentSortOrder === null) {
            loadAdmins(); // сброс
        } else {
            loadAdmins(currentSortType, currentSortOrder);
        }
    });

    function updateSortIcon() {
        if (!currentSortType) {
            sortIcon.src = '/assets/filter/svg/sort-off.svg';
        } else if (currentSortOrder === null) {
            sortIcon.src = '/assets/filter/svg/sort-default.svg';
        } else if (currentSortOrder === 'asc') {
            sortIcon.src = '/assets/filter/svg/sort-asc.svg';
        } else {
            sortIcon.src = '/assets/filter/svg/sort-desc.svg';
        }
    }

    // dropdown выбора поля сортировки
    sortTypeBtn.addEventListener('click', () => {
        sortTypeMenu.classList.toggle('hidden');
    });
    document.addEventListener('click', e => {
        if (!sortTypeBtn.contains(e.target) && !sortTypeMenu.contains(e.target)) {
            sortTypeMenu.classList.add('hidden');
        }
    });

    document.querySelectorAll('.dropdown-item-custom').forEach(item => {
        item.addEventListener('click', () => {
            currentSortType = item.dataset.sort;
            sortTypeBtn.textContent = item.textContent;
            currentSortOrder = null;
            sortTypeMenu.classList.add('hidden');
            updateSortIcon();
            loadAdmins();  // при смене поля сразу грузим с новым primarySort
        });
    });

    // первичная загрузка
    loadAdmins();
});