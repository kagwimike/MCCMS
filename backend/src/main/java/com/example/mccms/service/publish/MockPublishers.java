package com.example.mccms.service.publish;

import com.example.mccms.model.Deliverable;
import org.springframework.stereotype.Service;

/**
 * MOCKED implementation for TikTok.
 * Out of scope for live integration this semester due to manual audit requirements.
 */
@Service
class TikTokPublisher implements PlatformPublisher {
    @Override
    public String publish(Deliverable deliverable) throws Exception {
        System.out.println("Simulating TikTok Post: " + deliverable.getCaption());
        return "https://tiktok.com/@user/video/MCCMS_MOCK_ID";
    }

    @Override
    public String getPlatformName() {
        return "TikTok";
    }
}
