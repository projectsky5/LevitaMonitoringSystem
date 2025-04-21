function formatCurrency(amount, withDecimals = true) {
    return new Intl.NumberFormat('ru-RU', {
        minimumFractionDigits: withDecimals ? 2 : 0,
        maximumFractionDigits: withDecimals ? 2 : 0
    }).format(amount) + ' ₽';
}

// Получение ID из URL, если указан
function getAdminIdFromUrl() {
    const match = window.location.pathname.match(/^\/dashboard\/(\d+)$/);
    return match ? match[1] : null;
}

const adminId = getAdminIdFromUrl();
const url = adminId ? `/api/admins/${adminId}/dashboard` : `/api/dashboard`;
fetch(url)
    .then(res => res.json())
    .then(data => {
        const income = data.currentIncome;
        const remaining = Math.max(0, data.remainingToPlan);
        const plan = Math.round(data.locationPlan);
        const percent = data.planCompletionPercent;
        const rawLocation = data.locationName;
        const formattedLocation = rawLocation.charAt(0).toUpperCase() + rawLocation.slice(1).toLowerCase();

        // Шапка
        document.getElementById('username').innerText = data.username;
        document.getElementById('locationName').innerText = formattedLocation;

        // Блок текущий доход
        document.getElementById('currentIncome').innerText = `≈ ${formatCurrency(income)}`;
        document.getElementById('remainingToPlan').innerText = `${formatCurrency(remaining, false)} до цели`;
        document.getElementById('yellowBar').style.width = `${percent}%`;
        document.getElementById('locationPlan').innerText = `План студии ${formatCurrency(plan, false)}`;
        document.getElementById('planPercent').innerText = `${percent.toFixed(1)}%`;

        // Если цель достигнута
        const goalAchieved = remaining === 0;
        let chartColors = ['#5C86F3', '#e5ecff']
        if (goalAchieved) {
            // меняем иконку
            document.querySelector(".center-icon").src = "/assets/dashboard/svg/Mountain-done.svg";

            // меняем цвет прогресс-бара
            document.querySelector(".kpi-progress").classList.add("goal-achieved");

            chartColors = ['#4ACA52', '#e5ecff']
        }

        // Блок Цифра дня
        document.getElementById('dailyFigure').innerText = formatCurrency(data.dailyFigure, false);
        document.getElementById('locationPlanBlock').innerText = formatCurrency(data.locationPlan, false);
        document.getElementById('actualIncome').innerText = formatCurrency(data.actualIncome, false);
        document.getElementById('remainingBlock').innerText = formatCurrency(remaining, false);

        // Блок Бонусы дня
        document.getElementById("dayBonusesAmount").innerText = formatCurrency(data.dayBonuses, false);

        // Блок Основная часть заработной платы
        document.getElementById("mainSalaryPart").innerText = formatCurrency(data.mainSalaryPart, false);

        // Блок Личная Выручка
        document.getElementById("personalRevenue").innerText = formatCurrency(data.personalRevenue, false);

        // Блок Максимальная дневная выручка
        document.getElementById("maxDailyRevenue").innerText = formatCurrency(data.maxDailyRevenue, false);

        // Блок Средняя выручка за смену
        document.getElementById("avgRevenuePerDay").innerText = formatCurrency(data.avgRevenuePerDay, false);

        // Блок Конверсия
        document.getElementById("conversionRate").innerText = `${data.conversionRate.toFixed(1)}%`;

        // Диаграмма
        const ctx = document.getElementById('pieChart').getContext('2d');
        new Chart(ctx, {
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
    });
