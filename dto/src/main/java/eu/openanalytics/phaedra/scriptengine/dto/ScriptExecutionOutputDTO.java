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
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import lombok.experimental.NonFinal;

@Value
@Builder
@With
@NonFinal
public class ScriptExecutionOutputDTO {

    @NonNull
    String inputId;

    @NonNull
    String output;

    @NonNull
    ResponseStatusCode statusCode;

    @NonNull
    String statusMessage;

    @NonNull
    Integer exitCode;

    public ScriptExecutionOutputDTO(@JsonProperty(value = "inputId", required = true) @NonNull String inputId,
                                    @JsonProperty(value = "output", required = true) @NonNull String output,
                                    @JsonProperty(value = "statusCode", required = true) @NonNull ResponseStatusCode statusCode,
                                    @JsonProperty(value = "statusMessage", required = true) @NonNull String statusMessage,
                                    @JsonProperty(value = "exitCode", required = true) @NonNull Integer exitCode) {
        this.inputId = inputId;
        this.output = output;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.exitCode = exitCode;
    }



}
