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
package eu.openanalytics.phaedra.phaedra2scriptengine.model.runtime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO holding all information that is part of the request to execute a script.
 */
public class ScriptExecutionInput {

    private final String id;
    private final String script;
    private final String input;
    private final String responseTopicSuffix;

    public ScriptExecutionInput(@JsonProperty(value = "id", required = true) String id,
                                @JsonProperty(value = "script", required = true) String script,
                                @JsonProperty(value = "input", required = true) String input,
                                @JsonProperty(value = "response_topic_suffix", required = true) String responseTopicSuffix) {
        this.id = id;
        this.script = script;
        this.input = input;
        this.responseTopicSuffix = responseTopicSuffix;
    }

    public String getInput() {
        return input;
    }

    public String getScript() {
        return script;
    }

    public String getResponseTopicSuffix() {
        return responseTopicSuffix;
    }

    public String getId() {
        return id;
    }
}
