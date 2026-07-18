import { projectApi } from '../modules/project-api.js';

const totalProjects = document.getElementById('totalProjects');
const pendingReviews = document.getElementById('pendingReviews');
const atRiskDeadlines = document.getElementById('atRiskDeadlines');
const projectList = document.getElementById('projectList');

const projectModal = document.getElementById('projectModal');
const closeModal = document.getElementById('closeModal');
const projectForm = document.getElementById('projectForm');
const newProjectBtn = document.getElementById('newProjectBtn');
const mobileNewProjBtn = document.getElementById('mobileNewProjBtn');

const editProjectModal = document.getElementById('editProjectModal');
const editProjectForm = document.getElementById('editProjectForm');

// Initialize Quill
const quill = new Quill('#projDescEditor', {
    theme: 'snow',
    placeholder: 'Define production goals...'
});

// Auth Check
const user = JSON.parse(localStorage.getItem('user'));
if (!user || !localStorage.getItem('token')) {
    window.location.href = 'login.html';
}

const welcomeUser = document.getElementById('welcomeUser');
if (welcomeUser) welcomeUser.textContent = `Welcome, ${user.email.split('@')[0]} 👋`;

if (user.role === 'ADMIN') {
    const adminSection = document.getElementById('adminSection');
    if (adminSection) adminSection.style.display = 'block';
}

// --- OAuth Orchestration ---
const connectYoutubeBtn = document.getElementById('connectYoutubeBtn');
if (connectYoutubeBtn) {
    connectYoutubeBtn.onclick = async () => {
        const res = await fetch('http://localhost:8080/api/oauth/youtube/connect', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        const data = await res.json();
        window.location.href = data.url;
    };
}

// Check for OAuth Callback
const urlParams = new URLSearchParams(window.location.search);
if (urlParams.get('platform') === 'youtube' && urlParams.get('code')) {
    const code = urlParams.get('code');
    fetch(`http://localhost:8080/api/oauth/youtube/callback/process?code=${code}`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    }).then(res => {
        if (res.ok) alert('Successfully connected to YouTube!');
        window.history.replaceState({}, document.title, window.location.pathname); // Clean URL
    });
}

// 🛡️ Guardrail: Only show "+ Create Project" for Creators
if (user.role !== 'CREATOR') {
    if (newProjectBtn) newProjectBtn.style.display = 'none';
    if (mobileNewProjBtn) mobileNewProjBtn.style.display = 'none';
}

// Initial Data Load
async function loadDashboard() {
    try {
        const stats = await projectApi.getStats();
        totalProjects.textContent = stats.totalProjects;
        pendingReviews.textContent = stats.pendingReviews;

        // Fetch real unread/risk notifications count
        const notifRes = await fetch('http://localhost:8080/api/notifications/unread-count', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        const notifData = await notifRes.json();
        atRiskDeadlines.textContent = notifData.count;

        const projects = await projectApi.getProjects();
        renderProjects(projects);
    } catch (err) {
        console.error(err);
    }
}

function renderProjects(projects) {
    projectList.innerHTML = projects.map(p => `
        <tr>
            <td data-label="Project Title"><span style="font-weight: 600; color: #fff;">${p.title}</span></td>
            <td data-label="Creator"><span class="badge badge-creator" style="background: rgba(129, 140, 248, 0.1); color: #818cf8;">${p.creatorName}</span></td>
            <td data-label="Status"><span class="badge" style="background: rgba(52, 211, 153, 0.1); color: #34d399;">Active</span></td>
            <td data-label="Progress">
                <div style="width: 100px; height: 4px; background: rgba(255,255,255,0.05); border-radius: 2px; overflow: hidden; margin-top: 8px;">
                    <div style="width: ${p.progressPercentage}%; height: 100%; background: var(--dash-accent);"></div>
                </div>
                <div style="font-size: 9px; color: var(--color-text-muted); margin-top: 4px;">${p.progressPercentage}% Complete</div>
            </td>
            <td data-label="Action" style="text-align: right;">
                <div class="flex gap-2" style="justify-content: flex-end;">
                    <button class="btn-premium-sm" onclick="window.location.href='project-detail.html?id=${p.id}'">View</button>
                    ${user.role === 'CREATOR' || user.role === 'ADMIN' ? `
                        <button class="btn-premium-sm" style="background: rgba(251, 113, 133, 0.1); color: #fb7185; border: none;" onclick="window.deleteProject(${p.id})">Delete</button>
                        <button class="btn-premium-sm" style="background: rgba(251, 191, 36, 0.1); color: #fbbf24; border: none;" onclick="window.editProject(${p.id}, '${p.title.replace(/'/g, "\\'")}', '${(p.description || '').replace(/'/g, "\\'").replace(/\n/g, "\\n")}')">Edit</button>
                    ` : ''}
                </div>
            </td>
        </tr>
    `).join('') || '<tr><td colspan="5" style="text-align: center; padding: 4rem; color: var(--color-text-muted);">No pipeline activity found.</td></tr>';
}

// Global functions for inline onclick handlers
window.deleteProject = async (id) => {
    if (!confirm('Are you sure you want to delete this project? This will remove all associated deliverables and comments.')) return;
    try {
        await fetch(`http://localhost:8080/api/projects/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        loadDashboard();
    } catch (err) { alert(err.message); }
};

window.editProject = (id, title, desc) => {
    const editProjId = document.getElementById('editProjId');
    const editProjTitle = document.getElementById('editProjTitle');
    const editProjDesc = document.getElementById('editProjDesc');

    if (editProjId) editProjId.value = id;
    if (editProjTitle) editProjTitle.value = title;
    if (editProjDesc) editProjDesc.value = desc;
    if (editProjectModal) editProjectModal.style.display = 'flex';
};

// Event Listeners
if (newProjectBtn) newProjectBtn.onclick = () => projectModal.style.display = 'flex';
if (mobileNewProjBtn) mobileNewProjBtn.onclick = () => projectModal.style.display = 'flex';
if (closeModal) closeModal.onclick = () => projectModal.style.display = 'none';

projectForm.onsubmit = async (e) => {
    e.preventDefault();
    const title = document.getElementById('projTitle').value;
    const description = quill.root.innerHTML;

    try {
        await projectApi.createProject(title, description);
        projectModal.style.display = 'none';
        projectForm.reset();
        quill.setContents([]);
        loadDashboard();
    } catch (err) {
        alert(err.message);
    }
};

editProjectForm.onsubmit = async (e) => {
    e.preventDefault();
    const id = document.getElementById('editProjId').value;
    const title = document.getElementById('editProjTitle').value;
    const description = document.getElementById('editProjDesc').value;

    try {
        await fetch(`http://localhost:8080/api/projects/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({ title, description })
        });
        editProjectModal.style.display = 'none';
        loadDashboard();
    } catch (err) { alert(err.message); }
};

// Menu Toggle
const menuToggle = document.getElementById('menuToggle');
const sidebar = document.getElementById('sidebar');
const overlay = document.getElementById('sidebarOverlay');
if (menuToggle) {
    menuToggle.onclick = () => {
        sidebar.classList.toggle('active');
        overlay.classList.toggle('active');
    };
    overlay.onclick = () => {
        sidebar.classList.remove('active');
        overlay.classList.remove('active');
    };
}

document.getElementById('logoutBtn').onclick = () => {
    localStorage.clear();
    window.location.href = 'login.html';
};

loadDashboard();
