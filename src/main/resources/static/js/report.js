import { csrfFetch } from "./csrf.js";

document.addEventListener('DOMContentLoaded', async () => {
    let operations = [];
    let rollbackTimeout = null;
    let rollbackInterval = null;
    let rollbackActive = false;

    const operationCounter = document.getElementById('operationCounter');
    const operationPopup   = document.getElementById('operationPopup');
    const operationList    = document.getElementById('operationList');
    const uploadButton     = document.querySelector('.btn-upload');

    //
    // === Построение UI для кнопки «Отменить» и таймера ===
    //
    const cancelButton = document.createElement('button');
    const timerSpan    = document.createElement('span');
    timerSpan.style.fontWeight = 'bold';
    timerSpan.style.color      = '#CD171A';
    timerSpan.style.display    = 'none';

    const rollbackContainer = document.createElement('div');
    rollbackContainer.classList.add('d-flex','align-items-center');
    rollbackContainer.style.gap = '12px';
    rollbackContainer.append(cancelButton, timerSpan);

    uploadButton.parentElement.prepend(rollbackContainer);
    cancelButton.textContent = 'Отменить';
    cancelButton.classList.add('btn','btn-outline-danger','fw-semibold');
    cancelButton.style.marginRight = '16px';
    cancelButton.style.display     = 'none';

    cancelButton.addEventListener('click', async () => {
        rollbackActive = false;
        cancelButton.style.display = 'none';
        timerSpan.style.display    = 'none';
        uploadButton.disabled      = false;
        uploadButton.classList.remove('disabled');

        try {
            await csrfFetch('/api/report/rollback', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ reportDate: getReportDate() })
            });
        } catch (e) {
            console.error("Rollback failed", e);
        } finally {
            clearTimeout(rollbackTimeout);
            clearInterval(rollbackInterval);
            rollbackTimeout  = rollbackInterval = null;
        }
    });

    async function loadReportStatus() {
        try {
            const res = await csrfFetch('/api/report/status');
            if (!res.ok) throw new Error('status fetch failed');
            const {submittedAt, rolledBackAt } = await res.json();
            // если есть отправка после сегодняшнего дедлайна и не сделан rollback
            rollbackActive = !!(submittedAt && rolledBackAt === null);
            if (rollbackActive) startRollbackTimerUntil0200();
        } catch (e) {
            console.warn('Не удалось загрузить статус отчёта', e);
            rollbackActive = false;
        } finally {
            // отрисуем кнопки исходя из rollbackActive
            if (rollbackActive) {
                cancelButton.style.display = 'inline-block';
                timerSpan.style.display    = 'inline-block';
            } else {
                cancelButton.style.display = 'none';
                timerSpan.style.display    = 'none';
            }
            checkUploadAvailability();
        }
    }

    //
    // === Вычисление reportDate в формате "dd.MM - xx" ===
    //
    function getReportDate() {
        const parts = new Intl.DateTimeFormat('ru-RU', {
            timeZone: 'Europe/Moscow',
            hour12: false,
            year: 'numeric', month: '2-digit', day: '2-digit',
            hour: '2-digit', minute: '2-digit', second: '2-digit'
        }).formatToParts(new Date())
            .reduce((acc, p) => {
                if (p.type !== 'literal') acc[p.type] = p.value;
                return acc;
            }, {});

        const msk = new Date(
            `${parts.year}-${parts.month}-${parts.day}T` +
            `${parts.hour}:${parts.minute}:${parts.second}+03:00`
        );

        // если до 02:00 — считаем отчёт за предыдущий день
        if (msk.getHours() < 2) {
            msk.setDate(msk.getDate() - 1);
        }
        const d  = String(msk.getDate()).padStart(2, '0');
        const m  = String(msk.getMonth() + 1).padStart(2, '0');
        const wd = ['вс','пн','вт','ср','чт','пт','сб'][msk.getDay()];
        return `${d}.${m} - ${wd}`;
    }

    //
    // === Запуск таймера отката до 02:00 ===
    //
    function startRollbackTimerUntil0200() {
        const nowParts = new Intl.DateTimeFormat('ru-RU', {
            timeZone:'Europe/Moscow', hour12:false,
            year:'numeric', month:'2-digit', day:'2-digit',
            hour:'2-digit', minute:'2-digit', second:'2-digit'
        }).formatToParts(new Date())
            .reduce((acc, p) => { if (p.type !== 'literal') acc[p.type] = p.value; return acc; }, {});
        const mskNow = new Date(
            `${nowParts.year}-${nowParts.month}-${nowParts.day}T` +
            `${nowParts.hour}:${nowParts.minute}:${nowParts.second}+03:00`
        );

        // цель — ближайшие 02:00
        let endMSK = new Date(
            `${nowParts.year}-${nowParts.month}-${nowParts.day}T02:00:00+03:00`
        );
        if (mskNow.getHours() >= 2) {
            endMSK.setDate(endMSK.getDate() + 1);
        }

        const remainingMs = endMSK - mskNow;
        if (remainingMs <= 0) return;

        let sec = Math.floor(remainingMs / 1000);
        cancelButton.style.display = 'inline-block';
        timerSpan.style.display    = 'inline-block';

        const tick = () => {
            const h = String(Math.floor(sec / 3600)).padStart(2,'0');
            const m = String(Math.floor((sec % 3600)/60)).padStart(2,'0');
            const s = String(sec % 60).padStart(2,'0');
            timerSpan.textContent = `${h}:${m}:${s}`;
        };
        tick();
        rollbackInterval = setInterval(() => {
            sec--; tick();
            if (sec <= 0) clearInterval(rollbackInterval);
        }, 1000);
        rollbackTimeout = setTimeout(() => {
            cancelButton.style.display = 'none';
            timerSpan.style.display    = 'none';
            rollbackActive = false;
        }, remainingMs);
    }

    //
    // === Вспомогалки для операций ===
    //
    function clearAllInputs() {
        document.querySelectorAll('#reportView input, #reportView select')
            .forEach(i => i.tagName==='SELECT' ? i.selectedIndex=0 : i.value='');
    }
    function updateOperationList() {
        operationList.innerHTML = '';
        operations.forEach((op, i) => {
            const li = document.createElement('li');
            li.className = 'mb-3';
            li.innerHTML = `
        <div class="d-flex justify-content-between align-items-center mb-1">
          <strong>Операция ${i+1}</strong>
          <button class="btn-close-operation"
            onclick="removeOperation(${i});event.stopPropagation();">&times;</button>
        </div>
        <div>Тип: <strong>${op.type}</strong></div>
        <div>Сумма: <strong>${op.amount}₽</strong></div>
        <div>Касса / РС: <strong>${op.cashType}</strong></div>
        <div>Статья: <strong>${op.category}</strong></div>
        <div>Комментарий: <strong>${op.comment||'-'}</strong></div>`;
            operationList.append(li);
        });
    }
    window.removeOperation = idx => {
        operations.splice(idx,1);
        if (!operations.length) {
            operationPopup.classList.add('d-none');
            operationCounter.classList.add('d-none');
        }
        operationCounter.textContent = operations.length;
        updateOperationList();
    };
    document.getElementById('addOperationBtn').addEventListener('click', () => {
        const type        = document.getElementById('operationType').value;
        const amount      = parseFloat(document.getElementById('operationAmount').value);
        const cashType    = document.getElementById('operationTypeAccount').value;
        const category    = document.getElementById('operationCategory').value;
        const comment     = document.getElementById('operationComment').value;
        if (!type || isNaN(amount) || !cashType || !category) {
            return alert("Пожалуйста, заполните все поля операции.");
        }
        operations.push({type,amount,cashType,category,comment});
        operationCounter.textContent = operations.length;
        operationCounter.classList.remove('d-none');
        ['operationType','operationAmount','operationTypeAccount','operationCategory','operationComment']
            .forEach(id => document.getElementById(id).value = '');
    });
    operationCounter.addEventListener('click', () => {
        if (!operations.length) return;
        updateOperationList();
        operationPopup.classList.toggle('d-none');
    });
    document.addEventListener('click', e => {
        if (!operationPopup.contains(e.target) &&
            !operationCounter.contains(e.target)) {
            operationPopup.classList.add('d-none');
        }
    });

    //
    // === Обработчик «Загрузить отчёт» ===
    //
    uploadButton.addEventListener('click', async () => {
        rollbackActive = true;
        if (!isUploadAllowed()) {
            return alert("Загрузка доступна с 9:00 до 02:00 (МСК)");
        }

        const payload = {
            reportDate: getReportDate(),
            shift: {
                shiftStart: parseFloat(document.getElementById('shiftStart').value)||0,
                shiftEnd:   parseFloat(document.getElementById('shiftEnd').value)||0
            },
            trial: {
                trialCame:         parseInt(document.getElementById('trialCame').value)||0,
                trialBought:       parseInt(document.getElementById('trialBought').value)||0,
                trialBoughtAmount: parseFloat(document.getElementById('trialBoughtAmount').value)||0,
                trialPaid:         parseInt(document.getElementById('trialPaid').value)||0,
                trialPaidAmount:   parseFloat(document.getElementById('trialPaidAmount').value)||0,
                prepayment:        parseInt(document.getElementById('prepayment').value)||0,
                prepaymentAmount:  parseFloat(document.getElementById('prepaymentAmount').value)||0,
                surcharge:         parseInt(document.getElementById('surcharge').value)||0,
                surchargeAmount:   parseFloat(document.getElementById('surchargeAmount').value)||0
            },
            current: {
                finished:         parseInt(document.getElementById('currentFinished').value)||0,
                extended:         parseInt(document.getElementById('currentExtended').value)||0,
                extendedAmount:   parseFloat(document.getElementById('currentExtendedAmount').value)||0,
                upgrades:         parseInt(document.getElementById('currentUpgrades').value)||0,
                upgradeAmount:    parseFloat(document.getElementById('currentUpgradesAmount').value)||0,
                returned:         parseInt(document.getElementById('currentReturned').value)||0,
                returnedAmount:   parseFloat(document.getElementById('currentReturnedAmount').value)||0,
                prepayment:       parseInt(document.getElementById('currentPrepayment').value)||0,
                prepaymentAmount: parseFloat(document.getElementById('currentPrepaymentAmount').value)||0,
                surcharge:        parseInt(document.getElementById('currentSurcharge').value)||0,
                surchargeAmount:  parseFloat(document.getElementById('currentSurchargeAmount').value)||0,
                individual:       parseInt(document.getElementById('currentIndividual').value)||0,
                individualAmount: parseFloat(document.getElementById('currentIndividualAmount').value)||0,
                singleVisits:     parseInt(document.getElementById('currentOneTime').value)||0,
                singleVisitAmount:parseFloat(document.getElementById('currentOneTimeAmount').value)||0
            },
            operations
        };

        try {
            const res = await csrfFetch('/api/report', {
                method: 'POST',
                headers: {'Content-Type':'application/json'},
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw await res.json();
            // on success:
            clearAllInputs();
            operations = [];
            operationCounter.textContent = '0';
            operationCounter.classList.add('d-none');
            operationPopup.classList.add('d-none');
            startRollbackTimerUntil0200();
            await loadReportStatus();
        } catch (err) {
            console.error("Ошибка при отправке:", err);
            alert("Не удалось отправить отчёт. Подробнее в консоли.");
            rollbackActive = false;
        }
    });

    //
    // === Проверка разрешённого времени 09:00–02:00 ===
    //
    function isUploadAllowed() {
        const parts = new Intl.DateTimeFormat('ru-RU',{
            timeZone:'Europe/Moscow', hour12:false,
            hour:'2-digit', minute:'2-digit'
        }).formatToParts(new Date())
            .reduce((a,p)=>{ if(p.type!=='literal') a[p.type]=+p.value; return a },{});
        const h = parts.hour, m = parts.minute;
        const after9  = h > 9  || (h === 9 && m >= 0);
        const before2 = h < 2;
        // сюда попадаем если (после 9 утра) ИЛИ (до 2 ночи)
        return after9 || before2;
    }

    function checkUploadAvailability() {
        if (!isUploadAllowed() || rollbackActive) {
            uploadButton.disabled = true;
            uploadButton.classList.add('disabled');
            uploadButton.title = 'Загрузка доступна 09:00–02:00 (МСК)';
        } else {
            uploadButton.disabled = false;
            uploadButton.classList.remove('disabled');
            uploadButton.title = '';
        }
    }

    // checkUploadAvailability();
    // setInterval(checkUploadAvailability, 60_000);

    await loadReportStatus();
    setInterval(loadReportStatus, 60_000);
    checkUploadAvailability();
    setInterval(checkUploadAvailability, 60_000);

    //
    // === Помощь по разделам (оставляем как есть) ===
    //
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
        document.querySelectorAll('.help-button').forEach(btn => {
            btn.addEventListener('click', () => {
                const title = btn.dataset.section
                    || btn.closest('.report-block')?.querySelector('.section-title')?.textContent?.trim();
                alert(helpMessages[title] || 'Справка для этого раздела пока не доступна.');
            });
        });
    }, 0);
});