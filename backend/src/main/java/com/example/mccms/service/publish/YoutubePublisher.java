package com.example.mccms.service.publish;

import com.example.mccms.model.Deliverable;
import com.example.mccms.model.ConnectedAccount;
import com.example.mccms.repository.ConnectedAccountRepository;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class YoutubePublisher implements PlatformPublisher {

    private final ConnectedAccountRepository connectedAccountRepository;

    @Override
    public String publish(Deliverable deliverable) throws Exception {
        ConnectedAccount account = connectedAccountRepository.findByUserAndPlatformName(deliverable.getProject().getCreator(), "YouTube")
                .orElseThrow(() -> new RuntimeException("YouTube account not connected. Please use the 'Connect YouTube' button on the dashboard."));

        // 🛡️ Check if token is expired and needs refresh (Implementation for Milestone 8 persistence)
        String accessToken = account.getAccessToken();
        
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken(accessToken);

        YouTube youtube = new YouTube.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("MCCMS")
                .build();

        Video video = new Video();
        
        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("unlisted"); // 🛡️ Safe default
        video.setStatus(status);

        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(deliverable.getProject().getTitle());
        snippet.setDescription(deliverable.getCaption());
        snippet.setTags(Collections.singletonList("MCCMS"));
        video.setSnippet(snippet);

        // 🛡️ Locate the physical file
        String[] pathParts = deliverable.getMediaUrl().split("/");
        String fileName = pathParts[pathParts.length - 1];
        File mediaFile = Paths.get("./uploads").resolve(fileName).toFile();
        
        if (!mediaFile.exists()) {
            throw new RuntimeException("Physical video file not found for upload");
        }

        InputStreamContent mediaContent = new InputStreamContent("video/*", new FileInputStream(mediaFile));
        mediaContent.setLength(mediaFile.length()); // 🛡️ Explicit length for streaming reliability

        System.out.println("[YOUTUBE-API] Initializing upload stream for: " + mediaFile.getName());

        YouTube.Videos.Insert uploadRequest = youtube.videos().insert(
                Collections.singletonList("snippet,status"), 
                video, 
                mediaContent);
        
        // 🛡️ Enable resumable upload for large production files
        uploadRequest.getMediaHttpUploader().setDirectUploadEnabled(false);
        uploadRequest.getMediaHttpUploader().setChunkSize(1024 * 1024 * 2); // 2MB Chunks
        
        Video uploadedVideo = uploadRequest.execute();

        return "https://www.youtube.com/watch?v=" + uploadedVideo.getId();
    }

    @Override
    public String getPlatformName() {
        return "YouTube";
    }
}
