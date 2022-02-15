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
package eu.openanalytics.phaedra.scriptengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * POJO holding all information that is part of the request to execute a script.
 */
@Value
@Builder
@With
public class ScriptExecutionInputDTO {

    String id;
    String script;
    String input;
    String responseTopicSuffix;
    long queueTimestamp;

    public ScriptExecutionInputDTO(@JsonProperty(value = "id", required = true) String id,
                                   @JsonProperty(value = "script", required = true) String script,
                                   @JsonProperty(value = "input", required = true) String input,
                                   @JsonProperty(value = "responseTopicSuffix", required = true) String responseTopicSuffix,
                                   @JsonProperty(value = "queueTimestamp", required = true) long queueTimestamp) {
        this.id = id;
        this.script = script;
        this.input = input;
        this.responseTopicSuffix = responseTopicSuffix;
        this.queueTimestamp = queueTimestamp;
    }

}

