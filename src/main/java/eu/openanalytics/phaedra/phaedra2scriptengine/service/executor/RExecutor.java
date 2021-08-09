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

import eu.openanalytics.phaedra.phaedra2scriptengine.config.data.Config;
import eu.openanalytics.phaedra.phaedra2scriptengine.model.runtime.ScriptExecution;

import java.io.IOException;

/**
 * Implemention of {@link IExecutor} that can execute R scripts.
 */
public class RExecutor extends AbstractExecutor {

    public RExecutor(Config config) {
        super(config);
    }

    @Override
    protected String getFullScript(ScriptExecution scriptExecution) {
        return
            "fh <- file(\"input.json\")\n" +
                "input <- rjson::fromJSON(file=\"input.json\", simplify=TRUE)\n" +
                "close(fh)\n" +
                scriptExecution.getScriptExecutionInput().getScript() + "\n" +
                "fh <- file(\"output.json\")\n" +
                "writeLines(rjson::toJSON(list(output = output)), fh)\n" +
                "close(fh)\n";
    }

    @Override
    protected String getScriptName(ScriptExecution scriptExecution) {
        return "script.R";
    }

    protected int executeScript(ScriptExecution scriptExecution) throws WorkerException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();

        builder.command("/usr/bin/Rscript", "--vanilla", "script.R");

        builder.directory(scriptExecution.getWorkspace().toFile());

        try {
            Process process = builder.start();
            return process.waitFor();
        } catch (IOException e) {
            throw new WorkerException("Internal error during execution of the script", e);
        }

    }

}
