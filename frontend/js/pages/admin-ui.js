const API_BASE = 'http://localhost:8080/api/admin';

const getHeaders = () => ({
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.getItem('token')}`
});

const userList = document.getElementById('userList');
const settingsForm = document.getElementById('settingsForm');
const warningInput = document.getElementById('warningHours');

async function initAdmin() {
    // 1. Load Users
    try {
        const userRes = await fetch(`${API_BASE}/users`, { headers: getHeaders() });
        const users = await userRes.json();
        userList.innerHTML = users.map(u => `
            <tr>
                <td data-label="Name">${u.name}</td>
                <td data-label="Email">${u.email}</td>
                <td data-label="Role"><span class="badge" style="background: rgba(129, 140, 248, 0.1); color: #818cf8;">${u.role}</span></td>
                <td data-label="Status">
                    <span class="badge" style="background: ${u.status === 'ACTIVE' ? 'rgba(52, 211, 153, 0.1)' : 'rgba(251, 113, 133, 0.1)'}; color: ${u.status === 'ACTIVE' ? '#34d399' : '#fb7185'};">
                        ${u.status}
                    </span>
                </td>
                <td data-label="Action">
                    <button class="btn-premium-sm" onclick="window.toggleStatus(${u.id})">Toggle Status</button>
                </td>
            </tr>
        `).join('');
    } catch (err) { console.error(err); }

    // 2. Load Settings
    try {
        const settingsRes = await fetch(`${API_BASE}/settings`, { headers: getHeaders() });
        const settings = await settingsRes.json();
        const warning = settings.find(s => s.key === 'deadline_warning_hours');
        if (warning) warningInput.value = warning.value;
    } catch (err) { console.error(err); }
}

window.toggleStatus = async (id) => {
    if (!confirm('Are you sure you want to toggle this user status?')) return;
    try {
        await fetch(`${API_BASE}/users/${id}/status`, {
            method: 'PATCH',
            headers: getHeaders()
        });
        initAdmin();
    } catch (err) { alert(err.message); }
};

settingsForm.onsubmit = async (e) => {
    e.preventDefault();
    try {
        const res = await fetch(`${API_BASE}/settings/deadline_warning_hours`, {
            method: 'PUT',
            headers: getHeaders(),
            body: JSON.stringify({ value: warningInput.value })
        });
        if (res.ok) alert('Settings updated successfully!');
        else alert('Failed to update settings');
    } catch (err) { alert(err.message); }
};

initAdmin();
