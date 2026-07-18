const API_BASE_URL = 'http://localhost:8080/api/notifications';

const getHeaders = () => ({
    'Authorization': `Bearer ${localStorage.getItem('token')}`
});

const notifList = document.getElementById('notifList');

async function loadNotifications() {
    try {
        const response = await fetch(API_BASE_URL, { headers: getHeaders() });
        const notifications = await response.json();
        renderNotifications(notifications);
    } catch (err) {
        console.error(err);
    }
}

function renderNotifications(items) {
    notifList.innerHTML = items.map(n => `
        <div class="notif-card ${n.isRead ? 'read' : ''} ${n.type}">
            <div class="notif-content">
                <div class="notif-time">${new Date(n.createdAt).toLocaleString()}</div>
                <div class="notif-msg">${n.message}</div>
            </div>
            ${!n.isRead ? `<button class="btn-read" onclick="window.markRead(${n.id})">Mark Read</button>` : ''}
        </div>
    `).join('') || '<p class="text-muted">No notifications.</p>';
}

window.markRead = async (id) => {
    try {
        await fetch(`${API_BASE_URL}/${id}/read`, {
            method: 'PATCH',
            headers: getHeaders()
        });
        loadNotifications();
    } catch (err) {
        alert(err.message);
    }
};

loadNotifications();
