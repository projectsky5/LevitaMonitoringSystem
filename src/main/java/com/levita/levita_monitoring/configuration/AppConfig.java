package com.levita.levita_monitoring.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AppConfig {
    @Value("${sheets.thread-pool-size}")
    private int poolSize;

    @Bean(name="parserExecutor", destroyMethod="shutdown")
    public ExecutorService sheetsExecutor(){
        return Executors.newFixedThreadPool(poolSize);
    }
}
