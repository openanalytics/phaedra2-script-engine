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
package eu.openanalytics.phaedra.scriptengine.client.config;


import eu.openanalytics.phaedra.scriptengine.client.model.TargetRuntime;

import java.util.HashMap;
import java.util.Map;

// Must be configured as bean
public class ScriptEngineClientConfiguration {

    private final Map<String, TargetRuntime> targetRuntimes = new HashMap<>();

    private String clientName;

    public String getClientName() {
        return clientName;
    }

    public String getResponseQueueName() {
        return "scriptengine_output_for_" + clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void addTargetRuntime(String name, TargetRuntime targetRuntime) {
        targetRuntimes.put(name, targetRuntime);
    }

    public TargetRuntime getTargetRuntime(String targetName) {
        if (!targetRuntimes.containsKey(targetName)) {
            throw new IllegalArgumentException("Target not found with name " + targetName);
        }
        return targetRuntimes.get(targetName);
    }
}
