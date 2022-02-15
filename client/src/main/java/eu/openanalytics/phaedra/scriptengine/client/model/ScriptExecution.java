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
package eu.openanalytics.phaedra.scriptengine.client.model;

import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ScriptExecution {

    private final CompletableFuture<ScriptExecutionOutputDTO> output = new CompletableFuture<>();
    private final TargetRuntime targetRuntime;
    private final ScriptExecutionInputDTO scriptExecutionInput;

    public ScriptExecution(
        TargetRuntime targetRuntime,
        String script,
        String input,
        String responseTopicSuffix) {
        this.targetRuntime = targetRuntime;
        this.scriptExecutionInput = ScriptExecutionInputDTO.builder()
            .id(UUID.randomUUID().toString())
            .script(script)
            .input(input)
            .responseTopicSuffix(responseTopicSuffix)
            .queueTimestamp(System.currentTimeMillis())
            .build();
    }

    public ScriptExecutionInputDTO getScriptExecutionInput() {
        return scriptExecutionInput;
    }

    public TargetRuntime getTargetRuntime() {
        return targetRuntime;
    }

    public CompletableFuture<ScriptExecutionOutputDTO> getOutput() {
        return output;
    }

}
