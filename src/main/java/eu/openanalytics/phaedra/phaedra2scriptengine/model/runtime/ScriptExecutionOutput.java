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

/**
 * The output of the execution a script.
 */
public class ScriptExecutionOutput {

    private final ScriptExecutionInput scriptExecutionInput;
    private final String output;
    private final ResponseStatusCode statusCode;
    private final String statusMessage;
    private final int exitCode;

    public ScriptExecutionOutput(ScriptExecutionInput scriptExecutionInput, String output, ResponseStatusCode statusCode, String statusMessage, int exitCode) {
        this.scriptExecutionInput = scriptExecutionInput;
        this.output = output;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.exitCode = exitCode;
    }

    public String getOutput() {
        return output;
    }

    public ResponseStatusCode getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public int getExitCode() {
        return exitCode;
    }

    public ScriptExecutionInput getScriptExecutionInput() {
        return scriptExecutionInput;
    }
}
