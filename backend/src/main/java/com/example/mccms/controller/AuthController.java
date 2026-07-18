package com.example.mccms.controller;

import com.example.mccms.dto.AuthResponse;
import com.example.mccms.dto.LoginRequest;
import com.example.mccms.dto.RegisterRequest;
import com.example.mccms.model.User;
import com.example.mccms.repository.UserRepository;
import com.example.mccms.security.JwtProvider;
import com.example.mccms.service.AuditService;
import com.example.mccms.service.UserRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRegistrationService registrationService;
    private final JwtProvider tokenProvider;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        registrationService.registerUser(registerRequest);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(loginRequest.getEmail());
        
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
        auditService.log(user, "LOGIN", "USER", "SUCCESS");

        return ResponseEntity.ok(new AuthResponse(jwt, user.getEmail(), user.getRole().getName()));
    }
}
