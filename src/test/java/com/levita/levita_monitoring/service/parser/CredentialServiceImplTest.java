package com.levita.levita_monitoring.service.parser;

import com.levita.levita_monitoring.security.CredentialsGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialServiceImplTest {

    private final CredentialServiceImpl service = new CredentialServiceImpl();

    @Test
    void generate_delegatesToStaticGenerator() {
        String name = "Alice";
        String location = "HQ";
        Map<String, String> expected = Map.of("username", "alice_hq", "password", "secret");

        try (MockedStatic<CredentialsGenerator> mocks = mockStatic(CredentialsGenerator.class)) {
            mocks.when(() -> CredentialsGenerator.generateCredentials(name, location))
                    .thenReturn(expected);

            Map<String, String> actual = service.generate(name, location);

            assertSame(expected, actual, "Should return exactly what the static generator returns");
            mocks.verify(() -> CredentialsGenerator.generateCredentials(name, location), times(1));
        }
    }

    @Test
    void generate_returnsEmptyMapIfGeneratorReturnsEmpty() {
        String name = "Bob";
        String location = "Branch";
        Map<String, String> emptyMap = Map.of();

        try (MockedStatic<CredentialsGenerator> mocks = mockStatic(CredentialsGenerator.class)) {
            mocks.when(() -> CredentialsGenerator.generateCredentials(name, location))
                    .thenReturn(emptyMap);

            Map<String, String> actual = service.generate(name, location);

            assertTrue(actual.isEmpty(), "Should return an empty map when the generator does");
            mocks.verify(() -> CredentialsGenerator.generateCredentials(name, location), times(1));
        }
    }
}