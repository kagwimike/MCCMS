const API_BASE_URL = 'http://localhost:8080/api';

const getHeaders = () => ({
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.getItem('token')}`
});

export const projectApi = {
    async getStats() {
        const response = await fetch(`${API_BASE_URL}/dashboard/stats`, {
            headers: getHeaders()
        });
        if (!response.ok) throw new Error('Failed to fetch stats');
        return response.json();
    },

    async getProjects() {
        const response = await fetch(`${API_BASE_URL}/projects`, {
            headers: getHeaders()
        });
        if (!response.ok) throw new Error('Failed to fetch projects');
        return response.json();
    },

    async createProject(title, description) {
        const response = await fetch(`${API_BASE_URL}/projects`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({ title, description })
        });
        if (!response.ok) throw new Error('Failed to create project');
        return response.json();
    }
};
