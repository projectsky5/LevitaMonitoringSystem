// TODO: Логирование
// TODO: Маппинг и отправку в БД через сервисный слой
package com.levita.levita_monitoring.integration;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.levita.levita_monitoring.configuration.SpreadsheetConfig;
import com.levita.levita_monitoring.integration.enums.SheetsRanges;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.json.JsonFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class SheetsParser {

    private final List<SpreadsheetConfig> spreadsheetsConfig;

    @Autowired
    public SheetsParser(List<SpreadsheetConfig> spreadsheetsConfig) {
        this.spreadsheetsConfig = spreadsheetsConfig;
    }

    @PostConstruct
    public void init(){
        this.sheetsService = getSheetsService();
    }

    @Value("${application.name}")
    private static String applicationName;

    @Value("${google.sheets.credentials.file.path}")
    private String credentials;

    private Sheets sheetsService;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public void getDataFromSheets(){

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<?>> futures = new ArrayList<>();

        for(SpreadsheetConfig config : spreadsheetsConfig) {
            final String spreadsheetId = config.getSpreadsheetId();
            final List<SheetsRanges> ranges = config.getRanges();

            futures.add(executor.submit( () -> {
                for (SheetsRanges sheetRange : ranges) {
                    try{
                        ValueRange response = sheetsService.spreadsheets().values()
                                .get(spreadsheetId, sheetRange.getPropertyKey())
                                .execute();

                        List<List<Object>> values = response.getValues();
                        if(values == null || values.isEmpty()) {
                            System.out.println("Нет данных для таблицы " + spreadsheetId
                            + " и диапазона: " + sheetRange.getPropertyKey());
                        }
                        else{
                            List<Object> row = values.get(0);
                            if(!row.isEmpty()) {
                                String value = row.get(0).toString();
                                System.out.println("Таблица: " + spreadsheetId
                                + ", диапазон: " + sheetRange.getPropertyKey()
                                + ", значение: " + value);
                            }
                        }
                    } catch (IOException e){
                        throw new RuntimeException("Ошибка при запросе диапазона " + sheetRange.getPropertyKey(), e);
                    }
                }
            } ));
        }

        for (Future<?> future : futures) {
            try{
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();


    }


    public Sheets getSheetsService(){
        try{
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(credentials))
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

            return new Sheets.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(applicationName)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Ошибка при создании SheetsService для " + credentials, e);
        }
    }
}
