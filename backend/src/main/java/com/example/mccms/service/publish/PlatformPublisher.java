package com.example.mccms.service.publish;

import com.example.mccms.model.Deliverable;

/**
 * Common interface for all third-party platform publishing integrations.
 */
public interface PlatformPublisher {
    
    /**
     * Publishes the given deliverable to the third-party platform.
     * @param deliverable The content to publish
     * @return A message or URL from the platform indicating success
     * @throws Exception if publishing fails
     */
    String publish(Deliverable deliverable) throws Exception;
    
    /**
     * Returns the platform name this publisher handles (e.g., YouTube, TikTok).
     */
    String getPlatformName();
}
