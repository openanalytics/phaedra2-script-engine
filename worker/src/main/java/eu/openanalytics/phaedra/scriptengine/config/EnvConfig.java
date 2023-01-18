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
    private String poolName;
    private String version;
    private Integer prefetchCount = 250;
    private Integer consumers = 4;
    private String inputQueueName;
    private Integer heartbeatInterval;

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

        if (heartbeatInterval == null) {
            throw new IllegalArgumentException("Incorrect configuration detected: phaedra2.script-engine-worker.env.heartbeatInterval not set");
        }

        if (heartbeatInterval < 1) {
            throw new IllegalArgumentException("Incorrect configuration detected: phaedra2.script-engine-worker.env.heartbeatInterval must be greater than 1");
        }

        inputQueueName = String.format("scriptengine.input.%s.%s.%s", poolName, language, version);
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

    /**
     * @return the prefix of the topic used in output messages on the output exchange.
     */
    public String getOutputRoutingKeyPrefix() {
        return "scriptengine.output.";
    }

    /**
     * @return the number of messages to prefetch from the queue.
     */
    public Integer getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    /**
     * @return the number of consumers to create for the input queue. This corresponds to the number of workers working on incoming messages.
     */
    public Integer getConsumers() {
        return consumers;
    }

    public void setConsumers(Integer consumers) {
        this.consumers = consumers;
    }

    public String getInputQueueName() {
        return inputQueueName;
    }

    public void setHeartbeatInterval(Integer heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public Integer getHeartbeatInterval() {
        return heartbeatInterval;
    }

}


