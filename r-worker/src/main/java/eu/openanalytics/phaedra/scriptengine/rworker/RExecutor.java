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
package eu.openanalytics.phaedra.scriptengine.rworker;

import eu.openanalytics.phaedra.scriptengine.config.ExternalProcessConfig;
import eu.openanalytics.phaedra.scriptengine.exception.ScriptExecutionException;
import eu.openanalytics.phaedra.scriptengine.exception.WorkerException;
import eu.openanalytics.phaedra.scriptengine.executor.ExternalProcessExecutor;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutor;
import eu.openanalytics.phaedra.scriptengine.model.runtime.ScriptExecution;
import eu.openanalytics.phaedra.scriptengine.util.ProcessUtils;

/**
 * Implemention of {@link IExecutor} that can execute R scripts.
 */
public class RExecutor extends ExternalProcessExecutor {

	private static final String SCRIPT_WRAPPER =
			"input <- rjson::fromJSON(file=\"input.json\", simplify=TRUE)\n"
			+ "%s\n"
            + "fh <- file(\"output.json\")\n"
            + "writeLines(rjson::toJSON(list(output = output)), fh)\n"
            + "close(fh)\n";
	
    public RExecutor(ExternalProcessConfig config) {
        super(config);
    }

    @Override
    protected String getFullScript(ScriptExecution scriptExecution) {
        return String.format(SCRIPT_WRAPPER, scriptExecution.getScriptExecutionInput().getScript());
    }

    @Override
    protected String getScriptName(ScriptExecution scriptExecution) {
        return "script.R";
    }

    @Override
    protected void executeScript(ScriptExecution scriptExecution) throws ScriptExecutionException, WorkerException, InterruptedException {
    	String[] cmd = new String[] { "Rscript", "--vanilla", "script.R" };
    	String workingDir = scriptExecution.getWorkspace().toString();
    	try {
    		ProcessUtils.execute(cmd, workingDir, null, true, true);
    	} catch (RuntimeException e) {
    		throw new ScriptExecutionException(e.getMessage());
    	}
    }

}
