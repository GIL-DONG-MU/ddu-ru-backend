package com.dduru.gildongmu.fcm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${fcm.service-account-path}")
    private String serviceAccountPath;

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }
        try {
            InputStream serviceAccount = new ClassPathResource(serviceAccountPath).getInputStream();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            log.warn("Firebase 초기화 실패 — FCM 푸시 비활성화됩니다. ({})", e.getMessage());
        }
    }
}
