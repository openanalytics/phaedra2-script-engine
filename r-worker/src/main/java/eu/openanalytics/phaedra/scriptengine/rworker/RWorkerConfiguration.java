/**
 * Phaedra II
 *
 * Copyright (C) 2016-2023 Open Analytics
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
package eu.openanalytics.phaedra.scriptengine.rworker;

import eu.openanalytics.phaedra.scriptengine.config.ExternalProcessConfig;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutorRegistration;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RWorkerConfiguration {

    @Bean
    public IExecutorRegistration rExecutorRegistration() {
        return new IExecutorRegistration() {
            @Override
            public String getLanguage() {
                return "R";
            }

            @Override
            public IExecutor createExecutor() {
                return new RExecutor(externalProcessConfig());
            }

            public Boolean allowConcurrency() {
                return false;
            }
        };
    }

    @Bean
    public ExternalProcessConfig externalProcessConfig() {
        return new ExternalProcessConfig();
    }

}
