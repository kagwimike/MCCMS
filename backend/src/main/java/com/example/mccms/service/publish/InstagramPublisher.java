package com.example.mccms.service.publish;

import com.example.mccms.model.ConnectedAccount;
import com.example.mccms.model.Deliverable;
import com.example.mccms.repository.ConnectedAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class InstagramPublisher implements PlatformPublisher {

    private final ConnectedAccountRepository connectedAccountRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.public.url}")
    private String publicBaseUrl;

    @Override
    public String publish(Deliverable deliverable) throws Exception {
        ConnectedAccount account = connectedAccountRepository.findByUserAndPlatformName(deliverable.getProject().getCreator(), "Instagram")
                .orElseThrow(() -> new RuntimeException("Instagram/Meta account not connected"));

        String accessToken = account.getAccessToken();
        String igUserId = account.getRefreshToken();

        String mediaUrl = deliverable.getMediaUrl();
        String fileName = mediaUrl.substring(mediaUrl.lastIndexOf("/") + 1);
        String publicVideoUrl = publicBaseUrl + "/api/files/download/" + fileName;

        String containerUrl = String.format("https://graph.facebook.com/v19.0/%s/media", igUserId);
        
        Map<String, String> containerParams = Map.of(
            "media_type", "REELS",
            "video_url", publicVideoUrl,
            "caption", deliverable.getCaption(),
            "access_token", accessToken
        );

        ResponseEntity<Map> containerRes = restTemplate.postForEntity(containerUrl, containerParams, Map.class);
        String containerId = (String) containerRes.getBody().get("id");

        boolean ready = false;
        int attempts = 0;
        while (!ready && attempts < 10) {
            Thread.sleep(10000);
            String statusUrl = String.format("https://graph.facebook.com/v19.0/%s?fields=status_code&access_token=%s", containerId, accessToken);
            Map statusRes = restTemplate.getForObject(statusUrl, Map.class);
            String status = (String) statusRes.get("status_code");
            
            if ("FINISHED".equals(status)) ready = true;
            else if ("ERROR".equals(status)) throw new RuntimeException("Meta video processing failed");
            attempts++;
        }

        if (!ready) throw new RuntimeException("Instagram processing timed out");

        String publishUrl = String.format("https://graph.facebook.com/v19.0/%s/media_publish", igUserId);
        Map<String, String> publishParams = Map.of(
            "creation_id", containerId,
            "access_token", accessToken
        );

        ResponseEntity<Map> finalRes = restTemplate.postForEntity(publishUrl, publishParams, Map.class);
        String mediaId = (String) finalRes.getBody().get("id");

        return "https://www.instagram.com/reels/" + mediaId;
    }

    @Override
    public String getPlatformName() {
        return "Instagram";
    }
}
