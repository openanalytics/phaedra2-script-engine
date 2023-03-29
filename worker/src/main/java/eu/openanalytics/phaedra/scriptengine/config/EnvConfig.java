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
package eu.openanalytics.phaedra.scriptengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Contains all configuration related to the Environment of this worker.
 * That is, related to the runtime (R, Python ...) that is used.
 */
@Component
@ConfigurationProperties(prefix = "phaedra2.script-engine-worker.env")
public class EnvConfig {

    private String language;
    private Integer heartbeatInterval;

    @PostConstruct
    public void init() {
        if (language == null) {
            throw new IllegalArgumentException("Incorrect configuration detected: phaedra2.script-engine-worker.env.language not set");
        }
        if (heartbeatInterval == null) {
            throw new IllegalArgumentException("Incorrect configuration detected: phaedra2.script-engine-worker.env.heartbeatInterval not set");
        }
        if (heartbeatInterval < 1) {
            throw new IllegalArgumentException("Incorrect configuration detected: phaedra2.script-engine-worker.env.heartbeatInterval must be greater than 1");
        }
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setHeartbeatInterval(Integer heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public Integer getHeartbeatInterval() {
        return heartbeatInterval;
    }

}