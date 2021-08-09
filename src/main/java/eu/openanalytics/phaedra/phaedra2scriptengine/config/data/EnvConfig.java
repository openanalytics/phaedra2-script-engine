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
package eu.openanalytics.phaedra.phaedra2scriptengine.config.data;

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
    private String poolName;
    private String version;

    @PostConstruct
    public void init() {
        if (language == null) {
            throw new IllegalArgumentException("Incorrect configuration detected: phaedra2.script-engine-worker.env.language not set");
        }

        if (poolName == null) {
            throw new IllegalArgumentException("Incorrect configuration detected: phaedra2.script-engine-worker.env.pool-name not set");
        }

        if (version == null) {
            throw new IllegalArgumentException("Incorrect configuration detected: phaedra2.script-engine-worker.env.version not set");
        }
    }

    /**
     * @return the name of the language/runtime used by this worker. E.g. r, python, javascript.
     */
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return the pool-name this worker is part of. Typically, this will either be `fast-lane` or `background`.
     * This does not change the behavior of the worker itself.
     */
    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    /**
     * @return the version of the Environment/Runtime used by this worker.
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}


