package com.example.mccms.controller;

import com.example.mccms.service.OAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/youtube/connect")
    public ResponseEntity<Map<String, String>> connectYoutube() {
        return ResponseEntity.ok(Map.of("url", oAuthService.generateYoutubeAuthUrl()));
    }

    @GetMapping("/tiktok/connect")
    public ResponseEntity<Map<String, String>> connectTiktok() {
        return ResponseEntity.ok(Map.of("url", "https://www.tiktok.com/auth/authorize/"));
    }

    @GetMapping("/instagram/connect")
    public ResponseEntity<Map<String, String>> connectInstagram() {
        return ResponseEntity.ok(Map.of("url", "https://www.facebook.com/dialog/oauth/"));
    }

    @GetMapping("/callback/youtube")
    public void youtubeCallback(@RequestParam(required = false) String code, 
                                @RequestParam(required = false) String error,
                                HttpServletResponse response) throws IOException {
        if (error != null) {
            System.err.println("[OAuth-Error] Google returned error: " + error);
            response.sendRedirect("http://localhost:3000/dashboard.html?platform=youtube&error=" + error);
            return;
        }
        
        if (code == null) {
            System.err.println("[OAuth-Error] No code received from Google");
            response.sendRedirect("http://localhost:3000/dashboard.html?platform=youtube&error=no_code");
            return;
        }

        System.out.println("[OAuth-Success] Received code from Google. Redirecting to frontend...");
        response.sendRedirect("http://localhost:3000/dashboard.html?platform=youtube&code=" + code);
    }

    @PostMapping("/youtube/callback/process")
    public ResponseEntity<Void> processYoutubeCallback(@RequestParam String code, Authentication authentication) throws IOException {
        oAuthService.storeYoutubeToken(code, authentication.getName());
        return ResponseEntity.ok().build();
    }
}
