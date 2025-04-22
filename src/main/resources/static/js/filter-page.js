const sortButtons = document.querySelectorAll('.sort-btn');
let sortStates = {
    conversionRate: 'NONE',
    personalRevenue: 'NONE'
};

sortButtons.forEach(button => {
    button.addEventListener('click', () => {
        const sortType = button.getAttribute('data-sort');

        // Переключаем только состояние этой конкретной кнопки
        if (sortStates[sortType] === 'NONE') {
            sortStates[sortType] = 'ASC';
            button.classList.add('asc');
            button.classList.remove('desc');
        } else if (sortStates[sortType] === 'ASC') {
            sortStates[sortType] = 'DESC';
            button.classList.add('desc');
            button.classList.remove('asc');
        } else {
            sortStates[sortType] = 'NONE';
            button.classList.remove('asc', 'desc');
        }

        console.log('Current sort states:', sortStates);

        // Здесь потом добавим сортировку списка по sortStates, если нужно
    });
});
