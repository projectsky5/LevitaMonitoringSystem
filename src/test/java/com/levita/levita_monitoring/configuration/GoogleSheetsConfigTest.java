package com.levita.levita_monitoring.configuration;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GoogleSheetsConfigTest {

    @Test
    void googleSheetsBean_buildsWithMockedTransportAndCredential() throws Exception {
        // 1) Создаём временный файл, чтобы пропертя path была валидной
        File tempCreds = File.createTempFile("creds", ".json");
        tempCreds.deleteOnExit();

        try (MockedStatic<GoogleNetHttpTransport> gnt = mockStatic(GoogleNetHttpTransport.class);
             MockedStatic<GoogleCredential> gc   = mockStatic(GoogleCredential.class)) {

            NetHttpTransport fakeTransport = mock(NetHttpTransport.class);
            gnt.when(GoogleNetHttpTransport::newTrustedTransport)
                    .thenReturn(fakeTransport);

            GoogleCredential baseCred   = mock(GoogleCredential.class);
            GoogleCredential scopedCred = mock(GoogleCredential.class);
            gc.when(() -> GoogleCredential.fromStream(any(FileInputStream.class)))
                    .thenReturn(baseCred);
            when(baseCred.createScoped(List.of(SheetsScopes.SPREADSHEETS)))
                    .thenReturn(scopedCred);

            new ApplicationContextRunner()
                    .withUserConfiguration(GoogleSheetsConfig.class)
                    .withPropertyValues(
                            "google.sheets.credentials.file.path=" + tempCreds.getAbsolutePath(),
                            "application.name=MyApp"
                    )
                    .run(ctx -> {
                        // а) бин Sheets должен быть создан
                        assertThat(ctx).hasSingleBean(Sheets.class);
                        Sheets sheets = ctx.getBean(Sheets.class);

                        // б) applicationName подтянулось из проперти
                        assertThat(sheets.getApplicationName()).isEqualTo("MyApp");
                    });

            gnt.verify(GoogleNetHttpTransport::newTrustedTransport);
            gc.verify(() -> GoogleCredential.fromStream(any(FileInputStream.class)));
            verify(baseCred).createScoped(List.of(SheetsScopes.SPREADSHEETS));
        }
    }
}