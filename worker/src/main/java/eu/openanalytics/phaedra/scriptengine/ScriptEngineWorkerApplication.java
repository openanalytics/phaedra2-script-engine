/**
 * Phaedra II
 *
 * Copyright (C) 2016-2022 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.scriptengine;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import eu.openanalytics.phaedra.scriptengine.config.EnvConfig;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutor;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutorRegistration;

@EnableAsync
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ScriptEngineWorkerApplication {

    public final static String HEARTBEAT_EXCHANGE = "scriptengine_heartbeat";
    
    private final EnvConfig envConfig;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ScriptEngineWorkerApplication.class);
        app.run(args);
    }

    public ScriptEngineWorkerApplication(EnvConfig envConfig) {
        this.envConfig = envConfig;
    }

    @Bean
    public IExecutorRegistration executorRegistration(List<IExecutorRegistration> registrations) {
        logger.info(String.format("Available executors: %s", registrations.stream().map(ex -> String.format("lang: %s, concurrency: %s", ex.getLanguage(), ex.allowConcurrency())).collect(Collectors.toList())));

        for (var registration : registrations) {
            if (registration.getLanguage().equalsIgnoreCase(envConfig.getLanguage())) {
                logger.info(String.format("Configured to use the %s executor", registration.getLanguage()));
                return registration;
            }
        }

        throw new IllegalStateException(String.format("No matching IExecutorRegistration found, searching for %s", envConfig.getLanguage()));
    }

    @Bean
    public IExecutor scriptExecutor(IExecutorRegistration executorRegistration) {
        return executorRegistration.createExecutor();
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.initialize();
        return executor;
    }
}
