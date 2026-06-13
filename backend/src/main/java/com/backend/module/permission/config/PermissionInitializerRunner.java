package com.backend.module.permission.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.backend.module.permission.service.RolePermissionInitializer;

@Configuration
public class PermissionInitializerRunner {

    @Bean
    public CommandLineRunner initializePermissions(RolePermissionInitializer initializer) {
        return args -> initializer.initializeDefaults();
    }
}
