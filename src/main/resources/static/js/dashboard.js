let admins = [];
let currentAdminIndex = 0;
let chartInstance = null;

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
    })
    .catch(error => console.error('Ошибка при получении данных пользователя:', error));

// Загружаем всех админов для OWNER
function loadAdmins() {
    const urlParts = window.location.pathname.split('/');
    const urlUserId = urlParts[urlParts.length - 1];

    fetch('/api/admins')
        .then(response => response.json())
        .then(data => {
            admins = data;

            if (urlUserId) {
                const foundIndex = admins.findIndex(admin => admin.id === Number(urlUserId));
                if (foundIndex !== -1) {
                    currentAdminIndex = foundIndex; // Если нашли — переключаемся на этого админа
                } else {
                    console.warn('Админ с таким userId не найден');
                    currentAdminIndex = 0; // Фолбэк на первого
                }
            }

            updateNavigationButtons();
            loadAdminDashboard(admins[currentAdminIndex].id);
        })
        .catch(error => console.error('Ошибка при загрузке админов:', error));
}

//дашборд одного админа
function loadAdminDashboard(adminId) {
    fetch(`/api/admins/${adminId}/dashboard`)
        .then(response => response.json())
        .then(data => {
            renderDashboardData(data);
        })
        .catch(error => console.error('Ошибка при загрузке дашборда админа:', error));
}

// Для ADMIN — загружаются только свои данные
function loadOwnDashboard() {
    fetch('/api/dashboard')
        .then(response => response.json())
        .then(data => {
            renderDashboardData(data);
        })
        .catch(error => console.error('Ошибка при загрузке собственного дашборда:', error));
}

// Обновляем состояние кнопок
function updateNavigationButtons() {
    const prevButton = document.getElementById('prevAdmin');
    const nextButton = document.getElementById('nextAdmin');

    if (prevButton && nextButton) {
        prevButton.classList.toggle('disabled', currentAdminIndex === 0);
        nextButton.classList.toggle('disabled', currentAdminIndex === admins.length - 1);
    }
}

// События для стрелочек
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

    // Обновление шапки
    document.getElementById('username').innerText = data.username;
    document.getElementById('locationName').innerText = formattedLocation;

    // Обновление блока "Текущий доход"
    document.getElementById('currentIncome').innerText = `≈ ${formatCurrency(income)}`;
    document.getElementById('remainingToPlan').innerText = `${formatCurrency(remaining, false)} до цели`;
    document.getElementById('yellowBar').style.width = `${percent}%`;
    document.getElementById('locationPlan').innerText = `План студии ${formatCurrency(plan, false)}`;
    document.getElementById('planPercent').innerText = `${percent.toFixed(1)}%`;

    const goalAchieved = remaining === 0;
    let chartColors = ['#5C86F3', '#e5ecff'];

    if (goalAchieved) {
        const centerIcon = document.querySelector(".center-icon");
        const kpiProgress = document.querySelector(".kpi-progress");
        if (centerIcon) centerIcon.src = "/assets/dashboard/svg/Mountain-done.svg";
        if (kpiProgress) kpiProgress.classList.add("goal-achieved");
        chartColors = ['#4ACA52', '#e5ecff'];
    } else {
        const centerIcon = document.querySelector(".center-icon");
        const kpiProgress = document.querySelector(".kpi-progress");
        if (centerIcon) centerIcon.src = "/assets/dashboard/svg/Mountain.svg";
        if (kpiProgress) kpiProgress.classList.remove("goal-achieved");
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

    // Обновляем диаграмму
    const pieChartCanvas = document.getElementById('pieChart');
    if (chartInstance !== null) {
        chartInstance.destroy();
    }

    if (pieChartCanvas) {
        const ctx = pieChartCanvas.getContext('2d');
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

    // 👇 Также обновляем блок "Информация"
    const infoName = document.getElementById('infoName');
    const infoLocation = document.getElementById('infoLocation');
    const infoDate = document.getElementById('infoDate');

    if (infoName) infoName.innerText = data.username;
    if (infoLocation) infoLocation.innerText = formattedLocation;
    if (infoDate) {
        const now = new Date();
        const day = now.getDate().toString().padStart(2, '0');
        const month = (now.getMonth() + 1).toString().padStart(2, '0');
        const weekdays = ['вс', 'пн', 'вт', 'ср', 'чт', 'пт', 'сб'];
        const weekday = weekdays[now.getDay()];
        infoDate.innerText = `${day}.${month} - ${weekday}`;
    }
}

// Дропдаун
document.addEventListener("DOMContentLoaded", function () {
    const userIcon = document.querySelector(".user-icon");
    const adminMenu = document.getElementById("adminDropdown");
    const ownerMenu = document.getElementById("ownerDropdown");

    let userRole = null;
    let roleLoaded = false;

    userIcon.style.pointerEvents = "none";
    userIcon.style.opacity = "0.5";

    fetch("/api/me")
        .then(response => {
            if (!response.ok) {
                throw new Error("Ошибка загрузки данных пользователя");
            }
            return response.json();
        })
        .then(data => {
            userRole = data.role;
            roleLoaded = true;
            userIcon.style.pointerEvents = "auto";
            userIcon.style.opacity = "1";
            console.log("Роль пользователя:", userRole);
        })
        .catch(error => {
            console.error("Ошибка при получении роли:", error);
        });

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

    userIcon.addEventListener("click", showMenu);

    document.addEventListener("click", function (e) {
        if (!userIcon.contains(e.target) && !adminMenu.contains(e.target) && !ownerMenu.contains(e.target)) {
            adminMenu.classList.add("hidden");
            ownerMenu.classList.add("hidden");
        }
    });

    const logoutAdminBtn = document.getElementById("logoutAdmin");
    const logoutOwnerBtn = document.getElementById("logoutOwner");
    const refreshOwnerBtn = document.getElementById("refreshOwner");
    const filterOwnerBtn = document.getElementById("filterOwner");

    if (logoutAdminBtn) {
        logoutAdminBtn.addEventListener("click", () => window.location.href = "/dashboard/logout");
    }
    if (logoutOwnerBtn) {
        logoutOwnerBtn.addEventListener("click", () => window.location.href = "/dashboard/logout");
    }
    if (refreshOwnerBtn) {
        refreshOwnerBtn.addEventListener("click", () => alert("Обновить: заглушка"));
    }
    if (filterOwnerBtn) {
        filterOwnerBtn.addEventListener("click", () => window.location.href = "/dashboard/filter");
    }
});

function enableAdminFeatures() {
    document.querySelectorAll('.owner-only').forEach(el => el.classList.add('hidden-owner'));
}

document.addEventListener("DOMContentLoaded", () => {
    const toggleBtn = document.getElementById("toggleView");
    const kpiView = document.getElementById("kpiView");
    const reportView = document.getElementById("reportView");

    if (toggleBtn && kpiView && reportView) {
        toggleBtn.addEventListener("click", () => {
            const isKpiVisible = kpiView.style.display !== "none";
            kpiView.style.display = isKpiVisible ? "none" : "block";
            reportView.style.display = isKpiVisible ? "block" : "none";
        });
    }
});
