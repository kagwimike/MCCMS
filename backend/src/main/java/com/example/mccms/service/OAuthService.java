package com.example.mccms.service;

import com.example.mccms.model.ConnectedAccount;
import com.example.mccms.model.User;
import com.example.mccms.repository.ConnectedAccountRepository;
import com.example.mccms.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final ConnectedAccountRepository connectedAccountRepository;
    private final UserRepository userRepository;

    @Value("${youtube.client.id}")
    private String clientId;

    @Value("${youtube.client.secret}")
    private String clientSecret;

    @Value("${youtube.redirect.uri}")
    private String redirectUri;

    private static final String YOUTUBE_SCOPE = "https://www.googleapis.com/auth/youtube.upload";

    public String generateYoutubeAuthUrl() {
        return new GoogleAuthorizationCodeRequestUrl(
                clientId,
                redirectUri,
                Collections.singletonList(YOUTUBE_SCOPE))
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
    }

    @Transactional
    public void storeYoutubeToken(String code, String userEmail) throws IOException {
        System.out.println("[OAuth-Token] Exchanging code for tokens for user: " + userEmail);
        
        try {
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    clientId,
                    clientSecret,
                    Collections.singletonList(YOUTUBE_SCOPE))
                    .setAccessType("offline")
                    .build();

            GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
            
            System.out.println("[OAuth-Token] Successfully obtained access token");
            
            User user = userRepository.findByEmail(userEmail).orElseThrow();
            
            ConnectedAccount account = connectedAccountRepository.findByUserAndPlatformName(user, "YouTube")
                    .orElse(new ConnectedAccount());
            
            account.setUser(user);
            account.setPlatformName("YouTube");
            account.setAccessToken(response.getAccessToken());
            
            if (response.getRefreshToken() != null) {
                System.out.println("[OAuth-Token] Received new Refresh Token");
                account.setRefreshToken(response.getRefreshToken());
            }
            
            account.setExpiresAt(LocalDateTime.now().plusSeconds(response.getExpiresInSeconds()));
            
            connectedAccountRepository.save(account);
            System.out.println("[OAuth-Token] Account vault updated successfully");
        } catch (Exception e) {
            System.err.println("[OAuth-Token] Exchange failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
