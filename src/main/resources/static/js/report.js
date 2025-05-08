document.addEventListener('DOMContentLoaded', () => {
    let operations = [];
    let rollbackTimeout = null;
    let rollbackInterval = null;
    let rollbackActive = false;  // добавляем переменную состояния

    const operationCounter = document.getElementById('operationCounter');
    const operationPopup = document.getElementById('operationPopup');
    const operationList = document.getElementById('operationList');
    const uploadButton = document.querySelector('.btn-upload');

    // Создание кнопки "Отменить" динамически
    const cancelButton = document.createElement('button');
    // Создание таймера
    const timerSpan = document.createElement('span');
    timerSpan.style.fontWeight = 'bold';
    timerSpan.style.color = '#CD171A';
    timerSpan.style.display = 'none';

// Обёртка для кнопки и таймера
    const rollbackContainer = document.createElement('div');
    rollbackContainer.classList.add('d-flex', 'align-items-center');
    rollbackContainer.style.gap = '12px';
    rollbackContainer.appendChild(cancelButton);
    rollbackContainer.appendChild(timerSpan);

// Перед загрузкой
    uploadButton.parentElement.prepend(rollbackContainer);
    cancelButton.textContent = 'Отменить';
    cancelButton.classList.add('btn', 'btn-outline-danger', 'fw-semibold');
    cancelButton.style.marginRight = '16px';
    cancelButton.style.display = 'none';

    cancelButton.addEventListener('click', () => {
        rollbackActive = false;
        console.log('Rollback requested');
        cancelButton.style.display = 'none';
        timerSpan.style.display = 'none';
        uploadButton.disabled = false;
        uploadButton.classList.remove('disabled');

        alert('Отправка на /api/report/rollback');

        if (rollbackTimeout) {
            clearTimeout(rollbackTimeout);
            rollbackTimeout = null;
        }
        if (rollbackInterval) {
            clearInterval(rollbackInterval);
            rollbackInterval = null;
        }
    });

    function formatDateWithDay() {
        const now = new Date();
        const day = now.getDate().toString().padStart(2, '0');
        const month = (now.getMonth() + 1).toString().padStart(2, '0');
        const weekdayNames = ['вс', 'пн', 'вт', 'ср', 'чт', 'пт', 'сб'];
        const weekday = weekdayNames[now.getDay()];
        return `${day}.${month} - ${weekday}`;
    }

    window.removeOperation = function (index) {
        operations.splice(index, 1);
        if (operations.length === 0) {
            operationPopup.classList.add('d-none');
            operationCounter.classList.add('d-none');
        }
        operationCounter.textContent = operations.length;
        updateOperationList();
    };

    function clearAllInputs() {
        const inputs = document.querySelectorAll('#reportView input, #reportView select');
        inputs.forEach(input => {
            if (input.tagName === 'SELECT') {
                input.selectedIndex = 0;
            } else {
                input.value = '';
            }
        });
    }

    function updateOperationList() {
        operationList.innerHTML = '';
        operations.forEach((op, index) => {
            const entry = document.createElement('li');
            entry.classList.add('mb-3');
            entry.innerHTML = `
            <div class="d-flex justify-content-between align-items-center mb-1">
                <strong>Операция ${index + 1}</strong>
                <button class="btn-close-operation" onclick="removeOperation(${index}); event.stopPropagation();">&times;</button>
            </div>
            <div>Тип: <strong>${op.type}</strong></div>
            <div>Сумма: <strong>${op.amount}₽</strong></div>
            <div>Касса / РС: <strong>${op.cashType}</strong></div>
            <div>Статья: <strong>${op.category}</strong></div>
            <div>Комментарий: <strong>${op.comment || '-'}</strong></div>
        `;
            operationList.appendChild(entry);
        });
    }

    document.getElementById('addOperationBtn').addEventListener('click', () => {
        const type = document.getElementById('operationType').value;
        const amount = parseFloat(document.getElementById('operationAmount').value);
        const cashType = document.getElementById('operationTypeAccount').value;
        const category = document.getElementById('operationCategory').value;
        const comment = document.getElementById('operationComment').value;

        if (!type || isNaN(amount) || !cashType || !category) {
            alert("Пожалуйста, заполните все обязательные поля для операции.");
            return;
        }

        operations.push({ type, amount, cashType, category, comment });
        operationCounter.textContent = operations.length;
        operationCounter.classList.remove('d-none');

        document.getElementById('operationType').value = '';
        document.getElementById('operationAmount').value = '';
        document.getElementById('operationTypeAccount').value = '';
        document.getElementById('operationCategory').value = '';
        document.getElementById('operationComment').value = '';
    });

    operationCounter.addEventListener('click', () => {
        if (operations.length === 0) return;
        updateOperationList();
        operationPopup.classList.toggle('d-none');
    });

    document.addEventListener('click', (event) => {
        const isClickInside = operationPopup.contains(event.target) || operationCounter.contains(event.target);
        if (!isClickInside) {
            operationPopup.classList.add('d-none');
        }
    });

    function startRollbackTimerUntil2355() {
        const now = new Date();

        // Получаем время в часовом поясе Europe/Moscow в виде компонентов
        const mskTime = new Intl.DateTimeFormat('ru-RU', {
            timeZone: 'Europe/Moscow',
            hour12: false,
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        }).formatToParts(now).reduce((acc, part) => {
            if (part.type !== 'literal') acc[part.type] = part.value;
            return acc;
        }, {});

        // Преобразуем в объект Date с корректным московским временем
        const mskNow = new Date(
            `${mskTime.year}-${mskTime.month}-${mskTime.day}T${mskTime.hour}:${mskTime.minute}:${mskTime.second}+03:00`
        );

        const endMSK = new Date(
            `${mskTime.year}-${mskTime.month}-${mskTime.day}T23:55:00+03:00`
        );

        const remainingMs = endMSK - mskNow;

        if (remainingMs <= 0) {
            // Уже позже 23:55
            return;
        }

        let remaining = Math.floor(remainingMs / 1000);

        cancelButton.style.display = 'inline-block';
        timerSpan.style.display = 'inline-block';

        const updateTimerText = () => {
            const hours = String(Math.floor(remaining / 3600)).padStart(2, '0');
            const minutes = String(Math.floor((remaining % 3600) / 60)).padStart(2, '0');
            const seconds = String(remaining % 60).padStart(2, '0');
            timerSpan.textContent = `${hours}:${minutes}:${seconds}`;
        };

        updateTimerText();

        rollbackInterval = setInterval(() => {
            remaining--;
            updateTimerText();
            if (remaining <= 0) {
                clearInterval(rollbackInterval);
                rollbackInterval = null;
            }
        }, 1000);

        rollbackTimeout = setTimeout(() => {
            cancelButton.style.display = 'none';
            timerSpan.style.display = 'none';
            rollbackActive = false;
        }, remainingMs);
    }

    uploadButton.addEventListener('click', () => {
        rollbackActive = true;
        // Проверка времени перед выполнением
        if (!isUploadAllowed()) {
            alert("Загрузка отчёта доступна только с 9:00 до 23:55 по МСК");
            return;
        }

        const reportData = collectReportData();
        console.log('Report data:', reportData);

        operations = [];
        operationCounter.textContent = '0';
        operationCounter.classList.add('d-none');
        operationPopup.classList.add('d-none');

        clearAllInputs();

        // Блокируем кнопку и показываем "Отменить" и таймер
        uploadButton.disabled = true;
        uploadButton.classList.add('disabled');
        startRollbackTimerUntil2355();
    });

    function collectReportData() {
        const shiftStart = parseFloat(document.getElementById('shiftStart').value) || 0;
        const shiftEnd = parseFloat(document.getElementById('shiftEnd').value) || 0;

        const trial = {
            trialCame: parseInt(document.getElementById('trialCame').value) || 0,
            trialBought: parseInt(document.getElementById('trialBought').value) || 0,
            trialBoughtAmount: parseFloat(document.getElementById('trialBoughtAmount').value) || 0,
            trialPaid: parseInt(document.getElementById('trialPaid').value) || 0,
            trialPaidAmount: parseFloat(document.getElementById('trialPaidAmount').value) || 0,
            prepayment: parseInt(document.getElementById('prepayment').value) || 0,
            prepaymentAmount: parseFloat(document.getElementById('prepaymentAmount').value) || 0,
            surcharge: parseInt(document.getElementById('surcharge').value) || 0,
            surchargeAmount: parseFloat(document.getElementById('surchargeAmount').value) || 0
        };

        const current = {
            finished: parseInt(document.getElementById('currentFinished').value) || 0,
            extended: parseInt(document.getElementById('currentExtended').value) || 0,
            extendedAmount: parseFloat(document.getElementById('currentExtendedAmount').value) || 0,
            upgrades: parseInt(document.getElementById('currentUpgrades').value) || 0,
            upgradeAmount: parseFloat(document.getElementById('currentUpgradesAmount').value) || 0,
            returned: parseInt(document.getElementById('currentReturned').value) || 0,
            returnedAmount: parseFloat(document.getElementById('currentReturnedAmount').value) || 0,
            prepayment: parseInt(document.getElementById('currentPrepayment').value) || 0,
            prepaymentAmount: parseFloat(document.getElementById('currentPrepaymentAmount').value) || 0,
            surcharge: parseInt(document.getElementById('currentSurcharge').value) || 0,
            surchargeAmount: parseFloat(document.getElementById('currentSurchargeAmount').value) || 0,
            individual: parseInt(document.getElementById('currentIndividual').value) || 0,
            individualAmount: parseFloat(document.getElementById('currentIndividualAmount').value) || 0,
            singleVisits: parseInt(document.getElementById('currentOneTime').value) || 0,
            singleVisitAmount: parseFloat(document.getElementById('currentOneTimeAmount').value) || 0
        };

        return {
            shift: { shiftStart, shiftEnd },
            trial,
            current,
            operations
        };
    }

    document.getElementById('infoDate').textContent = formatDateWithDay();

    checkUploadAvailability();                         // Проверка при загрузке
    setInterval(checkUploadAvailability, 60000);       // Проверка каждую минуту

    const username = document.getElementById('username')?.textContent?.trim();
    const location = document.getElementById('locationName')?.textContent?.trim();

    if (username) document.getElementById('infoName').textContent = username;
    if (location) document.getElementById('infoLocation').textContent = location;

    function isUploadAllowed() {
        const nowUTC = new Date();
        const mskOffset = 3 * 60; // МСК = UTC+3
        const mskTime = new Date(nowUTC.getTime() + mskOffset * 60 * 1000);

        const hours = mskTime.getHours();
        const minutes = mskTime.getMinutes();

        // true, если загрузка разрешена
        const after9am = hours > 9 || (hours === 9 && minutes >= 0);
        const before2355 = hours < 23 || (hours === 23 && minutes < 55);

        return after9am && before2355;
    }

    function checkUploadAvailability() {
        const uploadButton = document.querySelector('.btn-upload');

        if (!isUploadAllowed() || rollbackActive) {
            uploadButton.disabled = true;
            uploadButton.classList.add('disabled');
            uploadButton.title = 'Загрузка отчёта доступна с 9:00 до 23:55 по МСК';
        } else {
            uploadButton.disabled = false;
            uploadButton.classList.remove('disabled');
            uploadButton.title = '';
        }
    }

    setTimeout(() => {
        const helpMessages = {
            'Касса в студии': 'Справка по разделу "Касса в студии":\n' +
                '1 — Укажите сумму в кассе на начало смены.\n' +
                '2 — Укажите сумму в кассе на конец смены.',
            'Пробные': 'Справка по разделу "Пробные":\n' +
                '1 — "Пришли" - количество человек, пришедших на пробное занятие в этот день (сумма).\n' +
                '2 — "Купили" - количество человек из суммы пришедших (см.пункт 1 "Пришли"), кто сразу оплатил абонемент полностью.\n' +
                '3 — "Сумма" - сумма за все абонементы, оплаченные сразу целиком (см.пункт 2 "Купили").\n' +
                '4 — "Оплат пр." - количество человек из суммы пришедших (см.пункт 1 "Пришли"), кто оплатил пробное занятие.\n' +
                '5 — "Сумма" - сумма за все оплаты пробных занятий в этот день (см.пункт 4 "Оплат пр").\n' +
                '6 — "Предоплаты" - количество человек из суммы пришедших (см.пункт 1 "Пришли"), кто оставил предоплату за абонемент.\n' +
                '7 — "Сумма" - сумма за все предоплаты за абонемент в этот день (см.пункт 6 "Предоплаты").\n' +
                '8 — "Доплаты" - количество человек из суммы пришедших (см.пункт 1 "Пришли"), кто кто доплатил за свой первый абонемент после предоплаты.\n' +
                '9 — "Сумма" - сумма за все доплаты за первый абонемент в этот день (см.пункт 8 "Доплаты").',
            'Текущие': 'Справка по разделу "Текущие":\n' +
                '1 — "Закончили" - сумма человек, у которых закончился абонемент в данную дату (по количеству+по сроку) (сумма).\n' +
                '2 — "Продлили" - количество человек из тех, у кого закончился (см.пункт 1 "Закончили"), кто продлил абонемент в день окончания (оплатил целиком именно сегодня) .\n' +
                '3 — "Сумма" - сумма за все оплаты продлений именно сегодня (см.пункт 2 "Продлили").\n' +
                '4 — "Апгрейды" - количество человек, которым сделали апгрейд (суммарное, включая и апгрейды с доплатой, и апгрейды с перерасчетом).\n' +
                '5 — "Сумма" - сумма за все апгрейды именно сегодня (см.пункт 4 "Апгрейды").\n' +
                '6 — "Вернулось" - количество человек, кто купил абонемент сегодня, но после перерыва, а именно не в день его окончания (даже если на следующий).\n' +
                '7 — "Сумма" - сумма за все оплаты абонементов не в день окончания (см.пункт 6 "Вернулось").\n' +
                '8 — "Предоплат" - количество человек из суммы тех, у кого закончился сегодня абонемент, но оплатили его не полностью, а оставили предоплату за него (см.пункт 1 "Закончили").\n' +
                '9 — "Сумма" - сумма за все предоплаты за абонемент в этот день (именно сегодня) (см.пункт 8 "Предоплаты").\n' +
                '10 — "Доплаты" - количество человек доплативших за свой абонемент (не первый) .\n' +
                '11 — "Сумма" - сумма за все доплаты за абонементы в этот день (см.пункт 10 "Доплаты").\n' +
                '12 — "Индивидуально" - количество купленных блоков индивидуальных занятий и сплит тренировок.\n' +
                '13 — "Сумма" - сумма за все блоки индивидуальных занятий и сплит тренировок (см.пункт 12 "Индивидуально").\n' +
                '14 — "Разовых" - количество купленных разовых посещений.\n' +
                '15 — "Сумма" - сумма за все разовые посещения (см.пункт 14 "Разовых").',
            'Операции': 'Справка по разделу "Операции":\n' +
                '1 — Выберите тип операции ("Расход" / "Приход").\n' +
                '2 — Укажите сумму на которую прошла операция.\n' +
                '3 — Выберите способ оплаты (наличные - наличные или перевод на карту; безнал - оплата через эквайринг).\n' +
                '4 — Выберите статью прихода/расхода.\n' +
                '5 — Дополните доход/расход информацией при необходимости.' +
                '\n\nКак работать с разделом:\n' +
                '1.1 — После ввода всех необходимых полей нажмите кнопку "Добавить операцию".\n' +
                '1.2 — Добавьте все прошедшие за день операции следуя пункту 1.1\n' +
                '1.3 — При нажатии на счетчик добавленных операций откроется список сохраненных (еще не отправленных) операций.\n' +
                '1.3.1 — Нажмите на крестик напротив Операция "номер операции" чтобы удалить эту операцию.'
        };

        document.querySelectorAll('.help-button').forEach(button => {
            button.addEventListener('click', () => {
                const sectionEl = button.closest('.report-block')?.querySelector('.section-title');
                const sectionTitle = button.dataset.section || sectionEl?.textContent?.trim();
                if (helpMessages[sectionTitle]) {
                    alert(helpMessages[sectionTitle]);
                } else {
                    alert('Справка для этого раздела пока не доступна.');
                }
            });
        });
    }, 0);
});
