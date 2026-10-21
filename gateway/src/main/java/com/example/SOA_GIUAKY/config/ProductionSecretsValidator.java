package com.example.SOA_GIUAKY.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ProductionSecretsValidator {
    public ProductionSecretsValidator(@Value("${app.admin.password}") String adminPassword,
                                     @Value("${app.internal-key}") String internalKey,
                                     @Value("${spring.profiles.active:}") String activeProfiles) {
        if (!Arrays.asList(activeProfiles.split(",")).contains("production")) return;
        if (adminPassword.length() < 14 || adminPassword.contains("replace-with")) {
            throw new IllegalStateException("APP_ADMIN_PASSWORD must contain at least 14 characters in production");
        }
        if (internalKey.length() < 32 || internalKey.contains("replace-with")) {
            throw new IllegalStateException("INTERNAL_SERVICE_KEY must contain at least 32 characters in production");
        }
    }
}
