package com.levita.levita_monitoring.configuration;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Configuration
public class GoogleSheetsConfig {

    @Value("${google.sheets.credentials.file.path}")
    private String credentialsPath;

    @Value("${application.name}")
    private String applicationName;

    @Bean
    public Sheets googleSheets() throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(credentialsPath))
                .createScoped(List.of(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(httpTransport, JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(applicationName)
                .build();
    }
}
