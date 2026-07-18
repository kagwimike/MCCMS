package com.example.mccms.config;

import com.example.mccms.model.Platform;
import com.example.mccms.model.Role;
import com.example.mccms.model.Stage;
import com.example.mccms.model.SystemSetting;
import com.example.mccms.repository.PlatformRepository;
import com.example.mccms.repository.RoleRepository;
import com.example.mccms.repository.StageRepository;
import com.example.mccms.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Seeds initial data: Roles, Stages, Platforms, and System Settings.
 * Uses a synchronized upsert strategy to avoid foreign key violations.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final StageRepository stageRepository;
    private final PlatformRepository platformRepository;
    private final SystemSettingRepository systemSettingRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        syncStages();
        seedPlatforms();
        seedSettings();
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(Arrays.asList(
                new Role(null, Role.ADMIN),
                new Role(null, Role.CREATOR),
                new Role(null, Role.REVIEWER)
            ));
            System.out.println("Seeded ROLES table.");
        }
    }

    /**
     * Non-destructive sync of the 14 workflow stages.
     */
    private void syncStages() {
        List<Stage> requiredStages = Arrays.asList(
            new Stage(null, "Idea", 1),
            new Stage(null, "Script Writing", 2),
            new Stage(null, "Planning", 3),
            new Stage(null, "Recording", 4),
            new Stage(null, "Import Raw Footage", 5),
            new Stage(null, "Editing", 6),
            new Stage(null, "Color Correction", 7),
            new Stage(null, "Audio Enhancement", 8),
            new Stage(null, "Thumbnail Design", 9),
            new Stage(null, "Short Clips Creation", 10),
            new Stage(null, "Review", 11),
            new Stage(null, "Final Export", 12),
            new Stage(null, "Upload", 13),
            new Stage(null, "Published", 14)
        );

        for (Stage req : requiredStages) {
            Optional<Stage> existing = stageRepository.findAll().stream()
                    .filter(s -> s.getName().equalsIgnoreCase(req.getName()))
                    .findFirst();

            if (existing.isEmpty()) {
                stageRepository.save(req);
                System.out.println("[SEED] Added missing stage: " + req.getName());
            } else {
                Stage current = existing.get();
                if (!current.getSortOrder().equals(req.getSortOrder())) {
                    current.setSortOrder(req.getSortOrder());
                    stageRepository.save(current);
                }
            }
        }
    }

    private void seedPlatforms() {
        if (platformRepository.count() == 0) {
            platformRepository.saveAll(Arrays.asList(
                new Platform(null, "YouTube", "REAL", "16:9", 43200, 5000),
                new Platform(null, "TikTok", "MOCKED", "9:16", 600, 150),
                new Platform(null, "Instagram", "MOCKED", "9:16", 90, 2200)
            ));
            System.out.println("Seeded platforms.");
        }
    }

    private void seedSettings() {
        if (systemSettingRepository.count() == 0) {
            systemSettingRepository.save(new SystemSetting(null, "deadline_warning_hours", "24"));
            System.out.println("Seeded SYSTEM_SETTINGS table.");
        }
    }
}
