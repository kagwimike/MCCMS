const API_BASE_URL = 'http://localhost:8080/api/analytics/summary';

const getHeaders = () => ({
    'Authorization': `Bearer ${localStorage.getItem('token')}`
});

async function initMoonAnalytics() {
    try {
        const response = await fetch(API_BASE_URL, { headers: getHeaders() });
        const data = await response.json();

        // 1. Map Core Stats
        document.getElementById('totalAssets').textContent = data.totalActiveDeliverables.toLocaleString();
        document.getElementById('gaugeValue').textContent = data.totalActiveDeliverables;
        document.getElementById('efficiencyScore').textContent = `${data.overallEfficiency}%`;

        // 2. Map Health Table (Status Distribution)
        renderHealthTable(data.statusDistribution, data.totalActiveDeliverables);

        // 3. Map Platform Breakdown
        renderPlatformBars(data.platformDistribution, data.totalActiveDeliverables);

        // 4. Map Charts
        renderGauge(data.overallEfficiency);
        renderMainTrendChart(data.consistencyData);
        renderCohortChart(); // Keeping as visual logic for now or mapping to creation dates

        // 5. Map Creator Performance
        renderCreatorTable(data.topPerformers);

    } catch (e) { console.error("Analytics Load Failure:", e); }
}

function renderHealthTable(dist, total) {
    const list = document.querySelector('.moon-table tbody');
    if (!list) return;

    list.innerHTML = Object.entries(dist).map(([status, count]) => {
        const pct = total > 0 ? ((count / total) * 100).toFixed(1) : 0;
        return `<tr><td>${status}</td><td>${pct}%</td><td>${count}</td></tr>`;
    }).join('') || '<tr><td colspan="3" class="text-muted">No data available</td></tr>';
}

function renderPlatformBars(dist, total) {
    const container = document.querySelector('.bar-stack');
    if (!container) return;

    const colors = { 'YouTube': '#6366f1', 'TikTok': '#10b981', 'Instagram': '#f43f5e' };

    container.innerHTML = Object.entries(dist).map(([platform, count]) => {
        const pct = total > 0 ? ((count / total) * 100).toFixed(1) : 0;
        return `
            <div class="bar-item">
                <div class="bar-meta"><span>${platform}</span><span>${pct}%</span></div>
                <div class="bar-bg"><div class="bar-fill" style="width: ${pct}%; background: ${colors[platform] || '#818cf8'};"></div></div>
            </div>
        `;
    }).join('');
}

function renderGauge(efficiency) {
    const ctx = document.getElementById('gaugeIncrease').getContext('2d');
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            datasets: [{
                data: [efficiency, 100 - efficiency],
                backgroundColor: ['#6366f1', 'rgba(255,255,255,0.03)'],
                borderWidth: 0,
                circumference: 180,
                rotation: 270,
            }]
        },
        options: { cutout: '85%', plugins: { legend: { display: false } } }
    });
}

function renderMainTrendChart(consistency) {
    const ctx = document.getElementById('mainAreaChart').getContext('2d');
    const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    const values = days.map(d => consistency[d] || 0);

    const grad = ctx.createLinearGradient(0, 0, 0, 300);
    grad.addColorStop(0, 'rgba(99, 102, 241, 0.2)');
    grad.addColorStop(1, 'transparent');

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: days,
            datasets: [{
                data: values,
                borderColor: '#6366f1',
                borderWidth: 3,
                fill: true,
                backgroundColor: grad,
                tension: 0.4,
                pointRadius: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { grid: { display: false }, ticks: { color: '#64748b', font: { size: 10 } } },
                y: { grid: { color: 'rgba(255,255,255,0.02)' }, ticks: { display: false }, beginAtZero: true }
            }
        }
    });
}

function renderCreatorTable(performers) {
    const list = document.getElementById('creatorList');
    if (!list) return;

    list.innerHTML = performers.map(p => `
        <tr><td>${p.name}</td><td>${p.efficiency}%</td><td>${p.volume}</td></tr>
    `).join('') || '<tr><td colspan="3" class="text-muted">No creator data</td></tr>';
}

function renderCohortChart() {
    const ctx = document.getElementById('cohortBarChart').getContext('2d');
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Week 1', 'Week 2', 'Week 3', 'Week 4'],
            datasets: [
                { label: 'Published', data: [5, 12, 8, 15], backgroundColor: '#6366f1', borderRadius: 4 },
                { label: 'Planned', data: [10, 8, 15, 5], backgroundColor: 'rgba(255,255,255,0.1)', borderRadius: 4 }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: { x: { stacked: true, grid: { display: false } }, y: { stacked: true, display: false } }
        }
    });
}

initMoonAnalytics();
