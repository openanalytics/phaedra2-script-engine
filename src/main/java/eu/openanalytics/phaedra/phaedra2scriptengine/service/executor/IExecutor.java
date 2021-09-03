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
package eu.openanalytics.phaedra.phaedra2scriptengine.service.executor;

import eu.openanalytics.phaedra.model.v2.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.phaedra2scriptengine.model.runtime.ScriptExecution;

/**
 * Interface for a service which can execute a {@link ScriptExecution}, producing a {@link ScriptExecutionOutputDTO}.
 */
public interface IExecutor {

    /**
     * Executes the script
     *
     * @param scriptExecution the script to execute
     * @return the output of the script
     * @throws InterruptedException when the thread is interrupted when waiting for the script to finish.
     */
    ScriptExecutionOutputDTO execute(ScriptExecution scriptExecution) throws InterruptedException;

}
