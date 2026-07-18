package com.example.mccms.service;

import com.example.mccms.dto.ProjectResponse;
import com.example.mccms.model.SystemSetting;
import com.example.mccms.model.User;
import com.example.mccms.repository.SystemSettingRepository;
import com.example.mccms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final SystemSettingRepository systemSettingRepository;

    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream().map(u -> Map.of(
            "id", (Object)u.getId(),
            "name", u.getName(),
            "email", u.getEmail(),
            "role", u.getRole().getName(),
            "status", u.getStatus()
        )).collect(Collectors.toList());
    }

    public List<SystemSetting> getSettings() {
        return systemSettingRepository.findAll();
    }

    @Transactional
    public void updateSetting(String key, String value) {
        SystemSetting setting = systemSettingRepository.findByKey(key)
                .orElse(new SystemSetting(null, key, value));
        setting.setValue(value);
        systemSettingRepository.save(setting);
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String newStatus = user.getStatus().equals("ACTIVE") ? "INACTIVE" : "ACTIVE";
        user.setStatus(newStatus);
        userRepository.save(user);
    }
}
