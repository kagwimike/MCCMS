import { deliverableApi } from '../modules/deliverable-api.js';

const urlParams = new URLSearchParams(window.location.search);
const projectId = urlParams.get('id');

const grid = document.getElementById('deliverablesGrid');
const addBtn = document.getElementById('addDeliverableBtn');
const modal = document.getElementById('deliverableModal');
const closeBtn = document.getElementById('closeDelivModal');
const form = document.getElementById('deliverableForm');

const editDelivModal = document.getElementById('editDelivModal');
const editDelivForm = document.getElementById('editDelivForm');

if (!projectId) {
    window.location.href = 'dashboard.html';
}

let STAGES = [];

async function loadStages() {
    try {
        const res = await fetch('http://localhost:8080/api/stages', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        STAGES = await res.json();
    } catch (e) { console.error("Failed to load stages:", e); }
}

async function loadDetails() {
    try {
        if (STAGES.length === 0) await loadStages();

        // 🛡️ Update Title & Description
        const projectsResponse = await fetch(`http://localhost:8080/api/projects`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        const projects = await projectsResponse.json();
        const project = projects.find(p => p.id == projectId);
        if (project) {
            document.getElementById('projTitleDisplay').textContent = project.title;
            document.getElementById('projDescDisplay').innerHTML = project.description || 'No description';
        }

        const deliverables = await deliverableApi.getForProject(projectId);
        renderDeliverables(deliverables);
        renderConnectionButtons(deliverables);
    } catch (err) {
        console.error(err);
    }
}

async function renderConnectionButtons(deliverables) {
    const area = document.getElementById('platformConnectionArea');
    if (!area) return;

    const hasYoutube = deliverables.some(d => d.platformName === 'YouTube');
    if (hasYoutube) {
        // Check if already connected via backend check (simplified for now)
        area.innerHTML = `
            <button class="btn-premium-sm" id="detailConnectYoutube" style="background: rgba(99, 102, 241, 0.1); color: var(--color-primary);">
                🔌 Reconnect YouTube
            </button>
        `;
        document.getElementById('detailConnectYoutube').onclick = async () => {
            const res = await fetch('http://localhost:8080/api/auth/oauth/youtube/connect', {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
            });
            const data = await res.json();
            window.location.href = data.url;
        };
    }
}

function renderDeliverables(deliverables) {
    // Guardrail: Disable "Add Platform" if all platforms already exist
    if (deliverables.length >= 3) {
        if (addBtn) addBtn.style.display = 'none';
    } else {
        if (addBtn) addBtn.style.display = 'block';
    }

    grid.innerHTML = deliverables.map(d => `
        <div class="deliverable-card">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <span class="platform-badge platform-${d.platformName.toLowerCase()}">${d.platformName}</span>
                <div class="flex gap-2">
                    <button class="btn-premium-sm" style="padding: 2px 6px; font-size: 10px; background: rgba(251, 113, 133, 0.1); color: #fb7185; border: none;" onclick="window.deleteDeliverable(${d.id})">Del</button>
                    <button class="btn-premium-sm" style="padding: 2px 6px; font-size: 10px; background: rgba(251, 191, 36, 0.1); color: #fbbf24; border: none;" onclick="window.editDeliverable(${d.id}, '${(d.caption || '').replace(/'/g, "\\'").replace(/\n/g, "\\n")}', '${d.mediaUrl || ''}')">Edit</button>
                </div>
            </div>
            <div class="kpi-value" style="font-size: 1.25rem;">${d.stageName}</div>
            <p style="margin-top: 0.5rem; font-size: 0.875rem;">${d.caption || 'No caption'}</p>

            <div class="flex flex-wrap gap-2" style="margin-top: 1rem;">
                <button class="btn-premium-sm" style="flex: 1; font-size: 10px;" onclick="window.openTasks(${d.id}, '${d.platformName}')">📝 Tasks</button>
                ${d.mediaUrl ? `<button class="btn-premium-sm" style="flex: 1; font-size: 10px;" onclick="window.open( '${d.mediaUrl}', '_blank')">👁️ Preview</button>` : ''}
                <button class="btn-premium-sm" style="flex: 1; font-size: 10px; background: rgba(99, 102, 241, 0.1); color: var(--color-primary);" onclick="window.connectPlatform('${d.platformName}')">🔌 Connect</button>
            </div>

            <div style="display: flex; gap: 0.5rem; align-items: center; margin-top: 1rem;">
                <select class="stage-select" style="margin-top: 0; flex: 1;" onchange="window.updateStage(${d.id}, this.value)">
                    ${STAGES.map(s => `<option value="${s.id}" ${s.name === d.stageName ? 'selected' : ''}>${s.name}</option>`).join('')}
                </select>
                ${(d.status === 'APPROVED' && d.stageName !== 'Published') ?
                    `<button class="btn-primary btn-sm" style="background: var(--color-success);" onclick="window.publish(${d.id})">Publish</button>` : ''}
            </div>

            ${d.status === 'PUBLISHED' ? `<p class="meta-info" style="color: var(--color-success); font-weight: bold;">LIVE: <a href="${d.mediaUrl}" target="_blank">View Post</a></p>` : ''}

            <div class="meta-info">
                Status: <span class="status-${d.status.toLowerCase()}">${d.status}</span><br>
                Last Update: ${new Date(d.stageUpdatedAt).toLocaleString()}
            </div>
        </div>
    `).join('');
}

// --- Task Management Logic (Epic 11) ---
let currentDelivIdForTasks = null;
const taskModal = document.getElementById('taskModal');
const taskList = document.getElementById('taskList');
const taskForm = document.getElementById('taskForm');

window.openTasks = async (delivId, platform) => {
    currentDelivIdForTasks = delivId;
    document.getElementById('taskModalTitle').textContent = `${platform} Checklist`;
    taskModal.style.display = 'flex';
    loadTasks();
};

async function loadTasks() {
    try {
        const res = await fetch(`http://localhost:8080/api/tasks/deliverable/${currentDelivIdForTasks}`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        const tasks = await res.json();
        renderTaskList(tasks);
    } catch (err) { console.error(err); }
}

function renderTaskList(tasks) {
    taskList.innerHTML = tasks.map(t => `
        <div class="flex items-center justify-between p-3" style="background: rgba(255,255,255,0.02); border-radius: 8px; margin-bottom: 8px; border: 1px solid var(--dash-border);">
            <div class="flex items-center gap-3">
                <input type="checkbox" ${t.isComplete ? 'checked' : ''} onchange="window.toggleTask(${t.id})" style="width: 18px; height: 18px; cursor: pointer;">
                <span style="${t.isComplete ? 'text-decoration: line-through; opacity: 0.5;' : ''}; font-size: 14px;">${t.title}</span>
            </div>
            <button onclick="window.deleteTask(${t.id})" style="background: none; border: none; color: var(--color-error); cursor: pointer; font-size: 12px;">✕</button>
        </div>
    `).join('') || '<p class="text-muted text-sm p-4">No tasks yet. Create your checklist below.</p>';
}

window.toggleTask = async (id) => {
    try {
        await fetch(`http://localhost:8080/api/tasks/${id}/toggle`, {
            method: 'PATCH',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        loadTasks();
    } catch (err) { console.error(err); }
};

window.deleteTask = async (id) => {
    try {
        await fetch(`http://localhost:8080/api/tasks/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        loadTasks();
    } catch (err) { console.error(err); }
};

taskForm.onsubmit = async (e) => {
    e.preventDefault();
    const title = document.getElementById('newTaskTitle').value;
    try {
        await fetch(`http://localhost:8080/api/tasks/deliverable/${currentDelivIdForTasks}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({ title, priority: 'MEDIUM' })
        });
        document.getElementById('newTaskTitle').value = '';
        loadTasks();
    } catch (err) { console.error(err); }
};

// Global functions for inline onclick handlers
window.deleteDeliverable = async (id) => {
    if (!confirm('Are you sure you want to delete this platform task?')) return;
    try {
        await fetch(`http://localhost:8080/api/deliverables/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        loadDetails();
    } catch (err) { alert(err.message); }
};

window.editDeliverable = (id, caption, url) => {
    document.getElementById('editDelivId').value = id;
    document.getElementById('editDelivCaption').value = caption;
    document.getElementById('editDelivUrl').value = url;
    editDelivModal.style.display = 'flex';
};

window.updateStage = async (id, stageId) => {
    const newStage = STAGES.find(s => s.id == stageId);
    if (!confirm(`Are you sure you want to move this task to '${newStage.name}'?`)) {
        loadDetails();
        return;
    }
    try {
        await deliverableApi.updateStage(id, parseInt(stageId));
        loadDetails();
    } catch (err) { alert(err.message); }
};

window.publish = async (id) => {
    if (!confirm('CRITICAL: This will post your content live. Are you sure?')) return;
    try {
        const response = await fetch(`http://localhost:8080/api/deliverables/${id}/publish`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        if (response.ok) {
            alert('Success: Content is now live!');
            loadDetails();
        } else {
            const err = await response.json();
            alert(`Error: ${err.message || 'Publishing failed'}`);
        }
    } catch (err) { alert(`System Error: ${err.message}`); }
};

window.connectPlatform = async (platformName) => {
    const endpoint = platformName.toLowerCase();
    try {
        const res = await fetch(`http://localhost:8080/api/oauth/${endpoint}/connect`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        const data = await res.json();
        if (data.url) {
            window.location.href = data.url;
        } else {
            alert(`Connect function for ${platformName} coming soon!`);
        }
    } catch (e) {
        alert(`Integration for ${platformName} is in the review process.`);
    }
};

// Event Listeners
if (addBtn) addBtn.onclick = () => modal.style.display = 'flex';
if (closeBtn) closeBtn.onclick = () => modal.style.display = 'none';

form.onsubmit = async (e) => {
    e.preventDefault();
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const statusDiv = document.getElementById('uploadStatus');
    const fileInput = document.getElementById('delivFile');
    let mediaUrl = document.getElementById('delivUrl').value;

    if (fileInput.files.length > 0) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Uploading...';
        statusDiv.style.color = 'var(--color-primary)';
        statusDiv.textContent = 'Transferring media to server...';

        const formData = new FormData();
        formData.append('file', fileInput.files[0]);

        try {
            const uploadRes = await fetch('http://localhost:8080/api/files/upload', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                },
                body: formData
            });

            if (!uploadRes.ok) {
                const errData = await uploadRes.json();
                throw new Error(errData.error || 'Upload failed');
            }

            const uploadData = await uploadRes.json();
            // Construct correct absolute URL for download
            const fileName = uploadData.url.split('/').pop();
            mediaUrl = `http://localhost:8080/api/files/download/${fileName}`;

            statusDiv.style.color = 'var(--color-success)';
            statusDiv.textContent = 'Upload Complete';
        } catch (err) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Add to Pipeline';
            statusDiv.style.color = 'var(--color-error)';
            statusDiv.textContent = 'Upload Error: ' + err.message;
            return alert('Critical: Media transfer interrupted. ' + err.message);
        }
    }

    const scheduledAtVal = document.getElementById('delivSchedule').value;
    const data = {
        platformId: parseInt(document.getElementById('platformSelect').value),
        caption: document.getElementById('delivCaption').value,
        mediaUrl: mediaUrl,
        scheduledAt: scheduledAtVal ? scheduledAtVal : null
    };

    console.log("[DEBUG] Submitting Deliverable Data:", data);

    // 🛡️ Pre-flight Duplicate Check (Frontend)
    const currentDelivs = Array.from(grid.querySelectorAll('.platform-badge')).map(el => el.textContent.trim());
    const selectedPlatformName = document.getElementById('platformSelect').options[document.getElementById('platformSelect').selectedIndex].text.split(' ')[0];

    if (currentDelivs.includes(selectedPlatformName)) {
        return alert(`Inconsistency: This project already has a ${selectedPlatformName} task. Each project can only have one task per platform.`);
    }

    try {
        await deliverableApi.add(projectId, data);
        modal.style.display = 'none';
        form.reset();
        statusDiv.textContent = '';
        loadDetails();
    } catch (err) {
        alert(err.message);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Add to Pipeline';
    }
};

editDelivForm.onsubmit = async (e) => {
    e.preventDefault();
    const id = document.getElementById('editDelivId').value;
    const caption = document.getElementById('editDelivCaption').value;
    const mediaUrl = document.getElementById('editDelivUrl').value;

    try {
        await fetch(`http://localhost:8080/api/deliverables/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({ caption, mediaUrl, platformId: 1 })
        });
        editDelivModal.style.display = 'none';
        loadDetails();
    } catch (err) { alert(err.message); }
};

// Menu & Sidebar
const menuToggle = document.getElementById('menuToggle');
const sidebar = document.getElementById('sidebar');
const overlay = document.getElementById('sidebarOverlay');
if (menuToggle) {
    menuToggle.onclick = () => { sidebar.classList.toggle('active'); overlay.classList.toggle('active'); };
    overlay.onclick = () => { sidebar.classList.remove('active'); overlay.classList.remove('active'); };
}

const user = JSON.parse(localStorage.getItem('user'));
if (user && user.role === 'ADMIN') {
    const adminSection = document.getElementById('adminSection');
    if (adminSection) adminSection.style.display = 'block';
}

document.getElementById('logoutBtn').onclick = () => {
    localStorage.clear();
    window.location.href = 'login.html';
};

loadDetails();
