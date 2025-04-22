let admins = [];
let currentAdminIndex = 0;
let chartInstance = null; // Для сохранения и очистки диаграммы

// Получение данных о текущем пользователе
fetch('/api/me')
    .then(response => response.json())
    .then(user => {
        if (user.role === 'OWNER') {
            document.querySelectorAll('.owner-only').forEach(el => el.style.display = 'block');
            loadAdmins();
        } else {
            enableAdminFeatures();
            loadOwnDashboard();
        }
    });

// Загружаем всех админов для OWNER
function loadAdmins() {
    fetch('/api/admins')
        .then(response => response.json())
        .then(data => {
            admins = data;
            updateNavigationButtons();
            loadAdminDashboard(admins[currentAdminIndex].id);
        });
}

// Загружаем дашборд одного админа
function loadAdminDashboard(adminId) {
    fetch(`/api/admins/${adminId}/dashboard`)
        .then(response => response.json())
        .then(data => {
            renderDashboardData(data);
        });
}

// Для ADMIN — загружаем только свои данные
function loadOwnDashboard() {
    fetch('/api/dashboard')
        .then(response => response.json())
        .then(data => {
            renderDashboardData(data);
        });
}

// Обновляем состояние кнопок (активна / неактивна)
function updateNavigationButtons() {
    const prevButton = document.getElementById('prevAdmin');
    const nextButton = document.getElementById('nextAdmin');

    prevButton.classList.toggle('disabled', currentAdminIndex === 0);
    nextButton.classList.toggle('disabled', currentAdminIndex === admins.length - 1);
}

// События для стрелочек
document.getElementById('prevAdmin').addEventListener('click', () => {
    if (currentAdminIndex > 0) {
        currentAdminIndex--;
        loadAdminDashboard(admins[currentAdminIndex].id);
        updateNavigationButtons();
    }
});

document.getElementById('nextAdmin').addEventListener('click', () => {
    if (currentAdminIndex < admins.length - 1) {
        currentAdminIndex++;
        loadAdminDashboard(admins[currentAdminIndex].id);
        updateNavigationButtons();
    }
});

// Форматирование рублей
function formatCurrency(amount, withDecimals = true) {
    return new Intl.NumberFormat('ru-RU', {
        minimumFractionDigits: withDecimals ? 2 : 0,
        maximumFractionDigits: withDecimals ? 2 : 0
    }).format(amount) + ' ₽';
}

// Рендер данных дашборда
function renderDashboardData(data) {
    const income = data.currentIncome;
    const remaining = Math.max(0, data.remainingToPlan);
    const plan = Math.round(data.locationPlan);
    const percent = data.planCompletionPercent;
    const rawLocation = data.locationName;
    const formattedLocation = rawLocation.charAt(0).toUpperCase() + rawLocation.slice(1).toLowerCase();

    // Обновляем шапку
    document.getElementById('username').innerText = data.username;
    document.getElementById('locationName').innerText = formattedLocation;

    // Обновляем блок "Текущий доход"
    document.getElementById('currentIncome').innerText = `≈ ${formatCurrency(income)}`;
    document.getElementById('remainingToPlan').innerText = `${formatCurrency(remaining, false)} до цели`;
    document.getElementById('yellowBar').style.width = `${percent}%`;
    document.getElementById('locationPlan').innerText = `План студии ${formatCurrency(plan, false)}`;
    document.getElementById('planPercent').innerText = `${percent.toFixed(1)}%`;

    const goalAchieved = remaining === 0;
    let chartColors = ['#5C86F3', '#e5ecff'];

    if (goalAchieved) {
        document.querySelector(".center-icon").src = "/assets/dashboard/svg/Mountain-done.svg";
        document.querySelector(".kpi-progress").classList.add("goal-achieved");
        chartColors = ['#4ACA52', '#e5ecff'];
    } else {
        document.querySelector(".center-icon").src = "/assets/dashboard/svg/Mountain.svg";
        document.querySelector(".kpi-progress").classList.remove("goal-achieved");
    }

    // Цифра дня
    document.getElementById('dailyFigure').innerText = formatCurrency(data.dailyFigure, false);
    document.getElementById('locationPlanBlock').innerText = formatCurrency(data.locationPlan, false);
    document.getElementById('actualIncome').innerText = formatCurrency(data.actualIncome, false);
    document.getElementById('remainingBlock').innerText = formatCurrency(remaining, false);

    // Остальные KPI
    document.getElementById("dayBonusesAmount").innerText = formatCurrency(data.dayBonuses, false);
    document.getElementById("mainSalaryPart").innerText = formatCurrency(data.mainSalaryPart, false);
    document.getElementById("personalRevenue").innerText = formatCurrency(data.personalRevenue, false);
    document.getElementById("maxDailyRevenue").innerText = formatCurrency(data.maxDailyRevenue, false);
    document.getElementById("avgRevenuePerDay").innerText = formatCurrency(data.avgRevenuePerDay, false);
    document.getElementById("conversionRate").innerText = `${data.conversionRate.toFixed(1)}%`;

    // Обновляем диаграмму (пересоздаём Chart.js)
    if (chartInstance !== null) {
        chartInstance.destroy();
    }

    const ctx = document.getElementById('pieChart').getContext('2d');
    chartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            datasets: [{
                data: goalAchieved ? [100, 0] : [percent, Math.max(0, 100 - percent)],
                backgroundColor: chartColors,
                borderWidth: 0
            }]
        },
        options: {
            cutout: '60%',
            responsive: false,
            plugins: {
                legend: { display: false },
                tooltip: { enabled: false }
            }
        }
    });
}
