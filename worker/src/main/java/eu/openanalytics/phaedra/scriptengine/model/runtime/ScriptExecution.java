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
package eu.openanalytics.phaedra.scriptengine.model.runtime;

import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;

import java.nio.file.Path;

/**
 * POJO holding information related to the execution of a script.
 * This information is only needed during execution of the script.
 */
public class ScriptExecution {

    private final ScriptExecutionInputDTO scriptExecutionInput;

    private Path workspace;

    public ScriptExecution(ScriptExecutionInputDTO scriptExecutionInput) {
        this.scriptExecutionInput = scriptExecutionInput;
    }

    public ScriptExecutionInputDTO getScriptExecutionInput() {
        return scriptExecutionInput;
    }

    public Path getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Path workspace) {
        this.workspace = workspace;
    }

}
