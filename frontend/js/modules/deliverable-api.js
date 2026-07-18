const API_BASE_URL = 'http://localhost:8080/api';

const getHeaders = () => ({
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.getItem('token')}`
});

export const deliverableApi = {
    async getForProject(projectId) {
        const response = await fetch(`${API_BASE_URL}/projects/${projectId}/deliverables`, {
            headers: getHeaders()
        });
        if (!response.ok) throw new Error('Failed to fetch deliverables');
        return response.json();
    },

    async add(projectId, data) {
        const response = await fetch(`${API_BASE_URL}/projects/${projectId}/deliverables`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.indexOf("application/json") !== -1) {
                const errorData = await response.json();
                console.error("Full Error Data:", errorData);
                // Try message, then details, then error (Spring default)
                const errorMsg = errorData.message || errorData.details || errorData.error || 'Failed to add deliverable';
                throw new Error(errorMsg);
            } else {
                const rawText = await response.text();
                throw new Error(`Server Error (${response.status}): ${rawText}`);
            }
        }
        return response.json();
    },

    async updateStage(deliverableId, stageId) {
        const response = await fetch(`${API_BASE_URL}/deliverables/${deliverableId}/stage`, {
            method: 'PATCH',
            headers: getHeaders(),
            body: JSON.stringify({ stageId })
        });
        if (!response.ok) throw new Error('Failed to update stage');
        return response.json();
    }
};
