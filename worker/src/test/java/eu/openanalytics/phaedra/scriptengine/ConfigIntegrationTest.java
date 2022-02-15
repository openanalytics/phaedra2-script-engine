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

import eu.openanalytics.phaedra.scriptengine.config.ExternalProcessConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the binding and validation of the configuration.
 */
@Testcontainers
class ConfigIntegrationTest {

    @Container
    public static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management")
        .withAdminPassword(null);

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void contextLoads() {
        this.contextRunner
            .withUserConfiguration(ScriptEngineWorkerApplication.class, Configuration.class)
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getMessage())
                    .contains("Incorrect configuration detected: phaedra2.script-engine-worker.env.language not set");
            });

        this.contextRunner
            .withPropertyValues("phaedra2.script-engine-worker.env.language=noop")
            .withUserConfiguration(ScriptEngineWorkerApplication.class, Configuration.class)
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getMessage())
                    .contains("Incorrect configuration detected: phaedra2.script-engine-worker.env.pool-name not set");
            });

        this.contextRunner
            .withPropertyValues(
                "phaedra2.script-engine-worker.env.language=noop",
                "phaedra2.script-engine-worker.env.pool-name=ast-lane")
            .withUserConfiguration(ScriptEngineWorkerApplication.class, Configuration.class)
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getMessage())
                    .contains("Incorrect configuration detected: phaedra2.script-engine-worker.env.version not set");
            });

        this.contextRunner
            .withPropertyValues(
                "spring.rabbitmq.host=" + rabbitMQContainer.getHost(),
                "spring.rabbitmq.port=" + rabbitMQContainer.getAmqpPort(),
                "phaedra2.script-engine-worker.env.language=noop",
                "phaedra2.script-engine-worker.env.pool-name=ast-lane",
                "phaedra2.script-engine-worker.env.version=v1")
            .withUserConfiguration(ScriptEngineWorkerApplication.class, Configuration.class)
            .withBean(ExternalProcessConfig.class, ExternalProcessConfig::new)
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getMessage())
                    .contains("Incorrect configuration detected: phaedra2.script-engine-worker.env.heartbeatInterval not set");
            });

        this.contextRunner
            .withPropertyValues(
                "spring.rabbitmq.host=" + rabbitMQContainer.getHost(),
                "spring.rabbitmq.port=" + rabbitMQContainer.getAmqpPort(),
                "phaedra2.script-engine-worker.env.language=noop",
                "phaedra2.script-engine-worker.env.pool-name=ast-lane",
                "phaedra2.script-engine-worker.env.version=v1",
                "phaedra2.script-engine-worker.env.heartbeatInterval=2")
            .withUserConfiguration(ScriptEngineWorkerApplication.class, Configuration.class)
            .withBean(ExternalProcessConfig.class, ExternalProcessConfig::new)
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getMessage())
                    .contains("Incorrect configuration detected: phaedra2.script-engine-worker.workspace not set");
            });



        this.contextRunner
            .withPropertyValues(
                "spring.rabbitmq.host=" + rabbitMQContainer.getHost(),
                "spring.rabbitmq.port=" + rabbitMQContainer.getAmqpPort(),
                "phaedra2.script-engine-worker.env.language=noop",
                "phaedra2.script-engine-worker.env.pool-name=ast-lane",
                "phaedra2.script-engine-worker.env.version=v1",
                "phaedra2.script-engine-worker.workspace=test",
                "phaedra2.script-engine-worker.env.heartbeatInterval=2")
            .withUserConfiguration(ScriptEngineWorkerApplication.class, Configuration.class)
            .withBean(ExternalProcessConfig.class, ExternalProcessConfig::new)
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getCause().getMessage())
                    .contains("Incorrect configuration detected: phaedra2.script-engine-worker.workspace must start and end with /");
            });

        this.contextRunner
            .withUserConfiguration(ScriptEngineWorkerApplication.class, Configuration.class)
            .withPropertyValues(
                "spring.rabbitmq.host=" + rabbitMQContainer.getHost(),
                "spring.rabbitmq.port=" + rabbitMQContainer.getAmqpPort(),
                "phaedra2.script-engine-worker.env.language=noop",
                "phaedra2.script-engine-worker.env.pool-name=ast-lane",
                "phaedra2.script-engine-worker.env.version=v1",
                "phaedra2.script-engine-worker.workspace=/test",
                "phaedra2.script-engine-worker.env.heartbeatInterval=2")
            .withBean(ExternalProcessConfig.class, ExternalProcessConfig::new)
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getCause().getMessage())
                    .contains("Incorrect configuration detected: phaedra2.script-engine-worker.workspace must start and end with /");
            });

        this.contextRunner
            .withUserConfiguration(ScriptEngineWorkerApplication.class, Configuration.class)
            .withPropertyValues(
                "spring.rabbitmq.host=" + rabbitMQContainer.getHost(),
                "spring.rabbitmq.port=" + rabbitMQContainer.getAmqpPort(),
                "phaedra2.script-engine-worker.env.language=noop",
                "phaedra2.script-engine-worker.env.pool-name=ast-lane",
                "phaedra2.script-engine-worker.env.version=v1",
                "phaedra2.script-engine-worker.workspace=/test/",
                "phaedra2.script-engine-worker.env.heartbeatInterval=2")
            .withBean(ExternalProcessConfig.class, ExternalProcessConfig::new)
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getCause().getMessage())
                    .contains("Incorrect configuration detected: phaedra2.script-engine-worker.workspace does not exists or is not a directory");
            });

        this.contextRunner
            .withUserConfiguration(ScriptEngineWorkerApplication.class, Configuration.class)
            .withPropertyValues(
                "spring.rabbitmq.host=" + rabbitMQContainer.getHost(),
                "spring.rabbitmq.port=" + rabbitMQContainer.getAmqpPort(),
                "phaedra2.script-engine-worker.env.language=xyz",
                "phaedra2.script-engine-worker.env.pool-name=ast-lane",
                "phaedra2.script-engine-worker.env.version=v1",
                "phaedra2.script-engine-worker.workspace=/tmp/",
                "phaedra2.script-engine-worker.env.heartbeatInterval=2",
                "spring.rabbitmq.host=" + rabbitMQContainer.getHost(),
                "spring.rabbitmq.port=" + rabbitMQContainer.getAmqpPort())
            .withBean(ExternalProcessConfig.class, ExternalProcessConfig::new)
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getCause().getCause().getCause().getCause().getCause().getMessage())
                    .contains("No matching IExecutorRegistration found, searching for xyz");
            });

        this.contextRunner
            .withUserConfiguration(ScriptEngineWorkerApplication.class, Configuration.class)
            .withPropertyValues(
                "spring.rabbitmq.host=" + rabbitMQContainer.getHost(),
                "spring.rabbitmq.port=" + rabbitMQContainer.getAmqpPort(),
                "phaedra2.script-engine-worker.env.language=noop",
                "phaedra2.script-engine-worker.env.pool-name=ast-lane",
                "phaedra2.script-engine-worker.env.version=v1",
                "phaedra2.script-engine-worker.workspace=/tmp/",
                "phaedra2.script-engine-worker.env.heartbeatInterval=2",
                "spring.rabbitmq.host=" + rabbitMQContainer.getHost(),
                "spring.rabbitmq.port=" + rabbitMQContainer.getAmqpPort())
            .withBean(ExternalProcessConfig.class, ExternalProcessConfig::new)
            .run(context -> {
                assertThat(context).hasNotFailed();
            });

    }

}
