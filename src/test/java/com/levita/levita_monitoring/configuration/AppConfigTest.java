package com.levita.levita_monitoring.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

class AppConfigTest {

    private final ApplicationContextRunner ctxRunner = new ApplicationContextRunner()
            .withUserConfiguration(AppConfig.class)
            .withPropertyValues("sheets.thread-pool-size=3");

    @Test
    void parserExecutorBean_ShouldExistAndHaveConfiguredPoolSize() {
        ctxRunner.run(ctx -> {
            assertThat(ctx).hasBean("parserExecutor");

            ExecutorService executor = ctx.getBean("parserExecutor", ExecutorService.class);
            assertThat(executor).isInstanceOf(ThreadPoolExecutor.class);
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
            assertThat(tpe.getCorePoolSize()).isEqualTo(3);
        });
    }
}