/**
 * Centralized UI utilities for consistent SaaS interaction patterns.
 */
export const ui = {
    /**
     * Standard error handler that parses the new ErrorDetails backend format.
     */
    handleError(err) {
        console.error('API Error:', err);
        const msg = err.message || 'An unexpected error occurred';
        alert(`Error: ${msg}`); // In production, this would be a nice toast component
    },

    /**
     * Show a standardized confirmation dialog.
     */
    confirm(message) {
        return window.confirm(message);
    },

    /**
     * Wraps an action in a safety confirmation if needed.
     */
    async safeAction(btn, message, actionFn) {
        if (message && !this.confirm(message)) return;

        try {
            this.setLoading(btn, true);
            await actionFn();
        } catch (err) {
            this.handleError(err);
        } finally {
            this.setLoading(btn, false);
        }
    },
    setLoading(btn, isLoading) {
        if (isLoading) {
            btn.dataset.originalText = btn.textContent;
            btn.disabled = true;
            btn.textContent = 'Processing...';
        } else {
            btn.disabled = false;
            btn.textContent = btn.dataset.originalText;
        }
    }
};
