/**
 * ContainerProxy
 *
 * Copyright (C) 2016-2021 Open Analytics
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
package eu.openanalytics.phaedra.phaedra2scriptengine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the binding and validation of the configuration.
 */
class ConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void contextLoads() {
        this.contextRunner
            .withUserConfiguration(ScriptEngineWorkerApplication.class)
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getMessage())
                    .contains("Incorrect configuration detected: phaedra2.script-engine-worker.env.language not set");
            });

        this.contextRunner
            .withPropertyValues("phaedra2.script-engine-worker.env.language=r")
            .withUserConfiguration(ScriptEngineWorkerApplication.class)
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getMessage())
                    .contains("Incorrect configuration detected: phaedra2.script-engine-worker.env.pool-name not set");
            });

        this.contextRunner
            .withPropertyValues(
                "phaedra2.script-engine-worker.env.language=r",
                "phaedra2.script-engine-worker.env.pool-name=ast-lane")
            .withUserConfiguration(ScriptEngineWorkerApplication.class)
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getMessage())
                    .contains("Incorrect configuration detected: phaedra2.script-engine-worker.env.version not set");
            });

        this.contextRunner
            .withPropertyValues(
                "phaedra2.script-engine-worker.env.language=r",
                "phaedra2.script-engine-worker.env.pool-name=ast-lane",
                "phaedra2.script-engine-worker.env.version=v1")
            .withUserConfiguration(ScriptEngineWorkerApplication.class)
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getMessage())
                    .contains("Incorrect configuration detected: phaedra2.script-engine-worker.workspace not set");
            });

        this.contextRunner
            .withPropertyValues(
                "phaedra2.script-engine-worker.env.language=r",
                "phaedra2.script-engine-worker.env.pool-name=ast-lane",
                "phaedra2.script-engine-worker.env.version=v1",
                "phaedra2.script-engine-worker.workspace=test")
            .withUserConfiguration(ScriptEngineWorkerApplication.class)
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getCause().getCause().getMessage())
                    .contains("Incorrect configuration detected: phaedra2.script-engine-worker.workspace must start and end with /");
            });

        this.contextRunner
            .withUserConfiguration(ScriptEngineWorkerApplication.class)
            .withPropertyValues(
                "phaedra2.script-engine-worker.env.language=r",
                "phaedra2.script-engine-worker.env.pool-name=ast-lane",
                "phaedra2.script-engine-worker.env.version=v1",
                "phaedra2.script-engine-worker.workspace=/test")
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getCause().getCause().getMessage())
                    .contains("Incorrect configuration detected: phaedra2.script-engine-worker.workspace must start and end with /");
            });

        this.contextRunner
            .withUserConfiguration(ScriptEngineWorkerApplication.class)
            .withPropertyValues(
                "phaedra2.script-engine-worker.env.language=r",
                "phaedra2.script-engine-worker.env.pool-name=ast-lane",
                "phaedra2.script-engine-worker.env.version=v1",
                "phaedra2.script-engine-worker.workspace=/test/")
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getCause().getCause().getMessage())
                    .contains("Incorrect configuration detected: phaedra2.script-engine-worker.workspace does not exists or is not a directory");
            });

        this.contextRunner
            .withUserConfiguration(ScriptEngineWorkerApplication.class)
            .withPropertyValues(
                "phaedra2.script-engine-worker.env.language=xyz",
                "phaedra2.script-engine-worker.env.pool-name=ast-lane",
                "phaedra2.script-engine-worker.env.version=v1",
                "phaedra2.script-engine-worker.workspace=/tmp/")
            .run(context -> {
                assertThat(context)
                    .hasFailed();
                assertThat(context.getStartupFailure().getCause().getCause().getCause().getCause().getMessage())
                    .contains("Unsupported language found xyz");
            });

        this.contextRunner
            .withUserConfiguration(ScriptEngineWorkerApplication.class)
            .withPropertyValues(
                "phaedra2.script-engine-worker.env.language=r",
                "phaedra2.script-engine-worker.env.pool-name=ast-lane",
                "phaedra2.script-engine-worker.env.version=v1",
                "phaedra2.script-engine-worker.workspace=/tmp/")
            .run(context -> {
                assertThat(context).hasNotFailed();
            });

    }

}
