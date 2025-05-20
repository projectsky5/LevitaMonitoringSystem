package com.levita.levita_monitoring.service.parser;

import java.util.Map;

public interface CredentialService {
    Map<String, String> generate(
            String name,
            String location
    );
}
