package com.levita.levita_monitoring.service.parser;

import com.levita.levita_monitoring.security.CredentialsGenerator;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CredentialServiceImpl implements CredentialService {

    @Override
    public Map<String, String> generate(String name, String location) {
        return CredentialsGenerator.generateCredentials(name, location);
    }
}
