import { projectApi } from '../modules/project-api.js';
import { deliverableApi } from '../modules/deliverable-api.js';

const queueList = document.getElementById('queueList');
const reviewModal = document.getElementById('reviewModal');
const commentHistory = document.getElementById('commentHistory');
const reviewText = document.getElementById('reviewText');

let currentDeliverableId = null;

async function loadQueue() {
    try {
        const projects = await projectApi.getProjects();
        let allDeliverables = [];
        for (const p of projects) {
            const delivs = await deliverableApi.getForProject(p.id);
            allDeliverables.push(...delivs.map(d => ({ ...d, projectTitle: p.title })));
        }
        renderQueue(allDeliverables);
    } catch (err) {
        console.error(err);
    }
}

function renderQueue(items) {
    queueList.innerHTML = items.map(i => `
        <tr>
            <td data-label="Project"><span style="font-weight: 600; color: #fff;">${i.projectTitle}</span></td>
            <td data-label="Platform"><span class="platform-badge platform-${i.platformName.toLowerCase()}">${i.platformName}</span></td>
            <td data-label="Stage">${i.stageName}</td>
            <td data-label="Status"><span class="badge status-${i.status.toLowerCase()}">${i.status}</span></td>
            <td data-label="Action" style="text-align: right;">
                <button class="btn-premium-sm" onclick="window.openReview(${i.id})">Review</button>
            </td>
        </tr>
    `).join('') || '<tr><td colspan="5" style="text-align: center; padding: 4rem; color: var(--color-text-muted);">The review queue is clear.</td></tr>';
}

window.openReview = async (id) => {
    currentDeliverableId = id;
    reviewModal.style.display = 'flex';
    reviewText.value = '';
    loadComments(id);
};

async function loadComments(id) {
    try {
        const response = await fetch(`http://localhost:8080/api/deliverables/${id}/comments`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        const comments = await response.json();
        commentHistory.innerHTML = comments.map(c => `
            <div class="comment-box" style="background: rgba(255,255,255,0.03); border-radius: 8px; padding: 12px; margin-bottom: 8px; border-left: 3px solid var(--color-primary);">
                <div class="comment-meta" style="font-size: 11px; opacity: 0.6; margin-bottom: 4px;">${c.reviewerName} • ${new Date(c.createdAt).toLocaleString()}</div>
                <div style="font-size: 13px;">${c.text}</div>
                <div style="font-size: 10px; font-weight: 700; margin-top: 4px; color: ${c.decision === 'APPROVED' ? 'var(--color-success)' : 'var(--color-error)'}">${c.decision}</div>
            </div>
        `).join('') || '<p class="text-muted">No previous feedback.</p>';
    } catch (err) {
        console.error(err);
    }
}

window.submitReview = async (decision) => {
    const text = reviewText.value;
    if (!text) return alert('Please enter feedback');

    try {
        const response = await fetch(`http://localhost:8080/api/deliverables/${currentDeliverableId}/review`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({ text, decision })
        });

        const contentType = response.headers.get("content-type");
        let data = {};
        if (contentType && contentType.indexOf("application/json") !== -1) {
            data = await response.json();
        }

        if (response.ok) {
            alert('Review submitted successfully');
            reviewModal.style.display = 'none';
            loadQueue();
        } else {
            alert(`Error: ${data.message || 'Submission failed'}`);
        }
    } catch (err) {
        alert(`System Error: ${err.message}`);
    }
};

loadQueue();
