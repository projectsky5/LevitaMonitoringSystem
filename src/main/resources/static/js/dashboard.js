import {csrfFetch} from "./csrf.js";

let admins = [];
let currentAdminIndex = 0;
let chartInstance = null;
// Получение данных о текущем пользователе
csrfFetch('/api/me')
    .then(response => response.json())
    .then(user => {
        if (user.role === 'OWNER') {
            document.querySelectorAll('.owner-only').forEach(el => el.style.display = 'block');
            loadAdmins();
        } else {
            enableAdminFeatures();
            loadOwnDashboard();
        }
    })
    .catch(error => console.error('Ошибка при получении данных пользователя:', error));

function loadAdmins() {
    const urlParts = window.location.pathname.split('/');
    const urlUserId = urlParts[urlParts.length - 1];

    csrfFetch('/api/admins')
        .then(response => response.json())
        .then(data => {
            admins = data;

            if (urlUserId) {
                const foundIndex = admins.findIndex(admin => admin.id === Number(urlUserId));
                currentAdminIndex = foundIndex !== -1 ? foundIndex : 0;
            }

            updateNavigationButtons();
            loadAdminDashboard(admins[currentAdminIndex].id);
        })
        .catch(error => console.error('Ошибка при загрузке админов:', error));
}

function loadAdminDashboard(adminId) {
    csrfFetch(`/api/admins/${adminId}/dashboard`)
        .then(response => response.json())
        .then(data => {
            renderDashboardData(data);
        })
        .catch(error => console.error('Ошибка при загрузке дашборда админа:', error));
}

function loadOwnDashboard() {
    csrfFetch('/api/dashboard')
        .then(response => response.json())
        .then(data => {
            renderDashboardData(data);
        })
        .catch(error => console.error('Ошибка при загрузке собственного дашборда:', error));
}

function updateNavigationButtons() {
    const prevButton = document.getElementById('prevAdmin');
    const nextButton = document.getElementById('nextAdmin');

    if (prevButton && nextButton) {
        prevButton.classList.toggle('disabled', currentAdminIndex === 0);
        nextButton.classList.toggle('disabled', currentAdminIndex === admins.length - 1);
    }
}

const prevButton = document.getElementById('prevAdmin');
const nextButton = document.getElementById('nextAdmin');

if (prevButton && nextButton) {
    prevButton.addEventListener('click', () => {
        if (currentAdminIndex > 0) {
            currentAdminIndex--;
            loadAdminDashboard(admins[currentAdminIndex].id);
            updateNavigationButtons();
        }
    });

    nextButton.addEventListener('click', () => {
        if (currentAdminIndex < admins.length - 1) {
            currentAdminIndex++;
            loadAdminDashboard(admins[currentAdminIndex].id);
            updateNavigationButtons();
        }
    });
}

function formatCurrency(amount, withDecimals = true) {
    return new Intl.NumberFormat('ru-RU', {
        minimumFractionDigits: withDecimals ? 2 : 0,
        maximumFractionDigits: withDecimals ? 2 : 0
    }).format(amount) + ' ₽';
}

function renderPieChart(percent, goalAchieved) {
    const pieChartCanvas = document.getElementById('pieChart');
    if (!pieChartCanvas) return;

    const ctx = pieChartCanvas.getContext('2d');
    if (!ctx) return;

    if (chartInstance?.destroy) chartInstance.destroy();

    const chartColors = goalAchieved ? ['#4ACA52', '#e5ecff'] : ['#5C86F3', '#e5ecff'];

    chartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            datasets: [{
                data: goalAchieved ? [100, 0] : [percent, 100 - percent],
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

function renderDashboardData(data) {
    const income = data.currentIncome;
    const remaining = Math.max(0, data.remainingToPlan);
    const plan = Math.round(data.locationPlan);
    const percent = data.planCompletionPercent;
    const formattedLocation = data.locationName.charAt(0).toUpperCase() + data.locationName.slice(1).toLowerCase();

    document.getElementById('username').innerText = data.username;
    document.getElementById('locationName').innerText = formattedLocation;

    document.getElementById('currentIncome').innerText = `≈ ${formatCurrency(income)}`;
    document.getElementById('remainingToPlan').innerText = `${formatCurrency(remaining, false)} до цели`;
    document.getElementById('yellowBar').style.width = `${percent}%`;
    document.getElementById('locationPlan').innerText = `План студии ${formatCurrency(plan, false)}`;
    document.getElementById('planPercent').innerText = `${percent.toFixed(1)}%`;

    const goalAchieved = remaining === 0;
    const centerIcon = document.querySelector(".center-icon");
    const kpiProgress = document.querySelector(".kpi-progress");

    if (centerIcon) centerIcon.src = goalAchieved ? "/assets/dashboard/svg/Mountain-done.svg" : "/assets/dashboard/svg/Mountain.svg";
    if (kpiProgress) kpiProgress.classList.toggle("goal-achieved", goalAchieved);

    document.getElementById('dailyFigure').innerText = formatCurrency(data.dailyFigure, false);
    document.getElementById('locationPlanBlock').innerText = formatCurrency(data.locationPlan, false);
    document.getElementById('actualIncome').innerText = formatCurrency(data.actualIncome, false);
    document.getElementById('remainingBlock').innerText = formatCurrency(remaining, false);

    document.getElementById("dayBonusesAmount").innerText = formatCurrency(data.dayBonuses, false);
    document.getElementById("mainSalaryPart").innerText = formatCurrency(data.mainSalaryPart, false);
    document.getElementById("personalRevenue").innerText = formatCurrency(data.personalRevenue, false);
    document.getElementById("maxDailyRevenue").innerText = formatCurrency(data.maxDailyRevenue, false);
    document.getElementById("avgRevenuePerDay").innerText = formatCurrency(data.avgRevenuePerDay, false);
    document.getElementById("conversionRate").innerText = `${data.conversionRate.toFixed(1)}%`;

    document.getElementById('infoName').innerText = data.username;
    document.getElementById('infoLocation').innerText = formattedLocation;

    const now = new Date();
    const day = String(now.getDate()).padStart(2, '0');
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const weekday = ['вс', 'пн', 'вт', 'ср', 'чт', 'пт', 'сб'][now.getDay()];
    document.getElementById('infoDate').innerText = `${day}.${month} - ${weekday}`;

    setTimeout(() => renderPieChart(percent, goalAchieved), 50);
}

function enableAdminFeatures() {
    document.querySelectorAll('.owner-only').forEach(el => el.classList.add('hidden-owner'));
}

document.addEventListener("DOMContentLoaded", () => {
    const kpiView = document.getElementById("kpiView");
    const reportView = document.getElementById("reportView");
    const adminMenu = document.getElementById("adminDropdown");
    const ownerMenu = document.getElementById("ownerDropdown");
    const userIcon = document.querySelector(".user-icon");
    const toggleButtons = [
        document.getElementById("toggleModeAdmin"),
        document.getElementById("toggleModeOwner")
    ];

    // true default
    let isKpiVisible = localStorage.getItem("isKpiVisible") !== "false";

    let userRole = null;
    let roleLoaded = false;

    // Установка начального состояния отображения
    kpiView.style.display = isKpiVisible ? "block" : "none";
    reportView.style.display = isKpiVisible ? "none" : "block";

    function updateToggleButtons() {
        toggleButtons.forEach(btn => {
            if (!btn) return;
            const img = btn.querySelector("img");
            const span = btn.querySelector("span");
            if (img) img.src = isKpiVisible ? "/assets/dashboard/svg/report.svg" : "/assets/dashboard/svg/dashboard.svg";
            if (span) span.textContent = isKpiVisible ? "Отчет" : "Дашборд";
        });
    }

    function toggleMode() {
        isKpiVisible = !isKpiVisible;
        localStorage.setItem("isKpiVisible", isKpiVisible);
        kpiView.style.display = isKpiVisible ? "block" : "none";
        reportView.style.display = isKpiVisible ? "none" : "block";
        updateToggleButtons();

        if (adminMenu) adminMenu.classList.add("hidden");
        if (ownerMenu) ownerMenu.classList.add("hidden");
    }

    // обработчики кнопок
    toggleButtons.forEach(btn => {
        if (btn) {
            btn.addEventListener("click", toggleMode);
        }
    });

    // клик по иконке юзера
    function showMenu() {
        if (!roleLoaded) return;

        if (userRole === "ADMIN") {
            adminMenu.classList.toggle("hidden");
            ownerMenu.classList.add("hidden");
        } else if (userRole === "OWNER") {
            ownerMenu.classList.toggle("hidden");
            adminMenu.classList.add("hidden");
        }
    }

    if (userIcon) {
        userIcon.addEventListener("click", showMenu);
    }

    // закрыть дропдаун извне
    document.addEventListener("click", function (e) {
        if (!userIcon.contains(e.target) && !adminMenu.contains(e.target) && !ownerMenu.contains(e.target)) {
            adminMenu.classList.add("hidden");
            ownerMenu.classList.add("hidden");
        }
    });

    // Загрузка роли
    userIcon.style.pointerEvents = "none";
    userIcon.style.opacity = "0.5";

    csrfFetch("/api/me")
        .then(response => response.json())
        .then(data => {
            userRole = data.role;
            roleLoaded = true;
            userIcon.style.pointerEvents = "auto";
            userIcon.style.opacity = "1";
            updateToggleButtons();
        })
        .catch(error => {
            console.error("Ошибка при получении роли:", error);
        });

    // Выход, фильтр, обновление
    const logoutAdminBtn = document.getElementById("logoutAdmin");
    const logoutOwnerBtn = document.getElementById("logoutOwner");
    const refreshOwnerBtn = document.getElementById("refreshOwner");
    const filterOwnerBtn = document.getElementById("filterOwner");

    if (logoutAdminBtn) {
        logoutAdminBtn.addEventListener('click', async () => {
            await csrfFetch('/dashboard/logout', { method: 'POST' });
            window.location.href = '/login?logout=true';
        });
    }
    if (logoutOwnerBtn) {
        logoutOwnerBtn.addEventListener('click', async () => {
            await csrfFetch('/dashboard/logout', { method: 'POST' });
            window.location.href = '/login?logout=true';
        });
    }
    if (refreshOwnerBtn) {
        refreshOwnerBtn.addEventListener('click', () => alert("Обновить: заглушка"));
    }
    if (filterOwnerBtn) {
        filterOwnerBtn.addEventListener('click', () => window.location.href = "/dashboard/filter");
    }
});