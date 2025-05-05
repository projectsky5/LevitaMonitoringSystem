document.addEventListener('DOMContentLoaded', function () {
    const sortButtons = document.querySelectorAll('.sort-btn');
    let sortStates = {
        conversionRate: 'NONE',
        personalRevenue: 'NONE'
    };
    let sortOrder = [];

    function loadAdmins(primarySort = null, primaryOrder = null, secondarySort = null, secondaryOrder = null) {
        let url = '/api/admins';
        const params = new URLSearchParams();

        if (primarySort && primaryOrder) {
            url = '/api/admins/sorted';
            params.append('primarySort', primarySort);
            params.append('primaryOrder', primaryOrder);
        }
        if (secondarySort && secondaryOrder) {
            params.append('secondarySort', secondarySort);
            params.append('secondaryOrder', secondaryOrder);
        }

        if (params.toString()) {
            url += `?${params.toString()}`;
        }

        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Ошибка загрузки админов');
                }
                return response.json();
            })
            .then(data => {
                renderAdmins(data);
            })
            .catch(error => {
                console.error('Ошибка:', error);
            });
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

    function getSortParams() {
        const activeSorts = sortOrder
            .filter(sortType => sortStates[sortType] !== 'NONE')
            .map(sortType => ({
                sort: sortType,
                order: sortStates[sortType].toLowerCase()
            }));

        const primary = activeSorts[0] || {};
        const secondary = activeSorts[1] || {};

        return {
            primarySort: primary.sort,
            primaryOrder: primary.order,
            secondarySort: secondary.sort,
            secondaryOrder: secondary.order
        };
    }

    sortButtons.forEach(button => {
        button.addEventListener('click', () => {
            const sortType = button.getAttribute('data-sort');

            if (sortStates[sortType] === 'NONE') {
                sortStates[sortType] = 'DESC';
                button.classList.add('asc');
                button.classList.remove('desc');
                if (!sortOrder.includes(sortType)) {
                    sortOrder.push(sortType);
                }
            } else if (sortStates[sortType] === 'DESC') {
                sortStates[sortType] = 'ASC';
                button.classList.add('desc');
                button.classList.remove('asc');
            } else {
                sortStates[sortType] = 'NONE';
                button.classList.remove('asc', 'desc');
                sortOrder = sortOrder.filter(type => type !== sortType);
            }

            const { primarySort, primaryOrder, secondarySort, secondaryOrder } = getSortParams();
            loadAdmins(primarySort, primaryOrder, secondarySort, secondaryOrder);
        });
    });

    loadAdmins();
});
