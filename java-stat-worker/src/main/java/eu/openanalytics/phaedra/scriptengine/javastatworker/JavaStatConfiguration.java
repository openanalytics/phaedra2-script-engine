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
package eu.openanalytics.phaedra.scriptengine.javastatworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutor;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutorRegistration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class JavaStatConfiguration {

    @Bean
    public IExecutorRegistration rExecutorRegistration(ObjectMapper objectMapper, List<StatCalculator> statCalculator) {
        return new IExecutorRegistration() {
            @Override
            public String getLanguage() {
                return "JavaStat";
            }

            @Override
            public IExecutor createExecutor() {
                return new JavaStatExecutor(objectMapper, statCalculator);
            }

            @Override
            public Boolean allowConcurrency() {
                return true;
            }
        };
    }

}
