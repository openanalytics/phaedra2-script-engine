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
package eu.openanalytics.phaedra.scriptengine.executor;

import eu.openanalytics.phaedra.scriptengine.config.ExternalProcessConfig;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.exception.WorkerException;
import eu.openanalytics.phaedra.scriptengine.model.runtime.ScriptExecution;
import eu.openanalytics.phaedra.scriptengine.service.ShutdownService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Abstract executor that contains the global logic for executing a script using an external process.
 * This can be used to implement {@link IExecutor} for different languages/runtimes.
 */
public abstract class ExternalProcessExecutor implements IExecutor {

    private final ExternalProcessConfig config;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ExternalProcessExecutor(ExternalProcessConfig config) {
        this.config = config;
    }

    @Override
    public ScriptExecutionOutputDTO execute(ScriptExecutionInputDTO scriptExecutionInput) throws InterruptedException {
        return execute(new ScriptExecution(scriptExecutionInput));
    }

    public ScriptExecutionOutputDTO execute(ScriptExecution scriptExecution) throws InterruptedException {
        try {
            setupEnv(scriptExecution);

            int exitCode = executeScript(scriptExecution);

            if (ShutdownService.isShuttingDown()) {
                logger.warn("Script marked as WORKER_INTERNAL_ERROR since worker is shutting down!");
                return new ScriptExecutionOutputDTO(scriptExecution.getScriptExecutionInput().getId(), "", ResponseStatusCode.WORKER_INTERNAL_ERROR, "Worker shutting down!", exitCode);
            }

            if (!checkOutput(scriptExecution)) {
                return new ScriptExecutionOutputDTO(scriptExecution.getScriptExecutionInput().getId(),
                    "", ResponseStatusCode.SCRIPT_ERROR, "Script did not create output file!", exitCode);
            }

            String output = readOutput(scriptExecution);

            return new ScriptExecutionOutputDTO(scriptExecution.getScriptExecutionInput().getId(),
                output, ResponseStatusCode.SUCCESS, "Ok", exitCode);

        } catch (WorkerException e) {
            e.printStackTrace();
            return new ScriptExecutionOutputDTO(scriptExecution.getScriptExecutionInput().getId(),
                "", ResponseStatusCode.WORKER_INTERNAL_ERROR, "An error occurred in the worker while processing the script.", 0);
        } finally {
            if (config.getCleanWorkspace()) {
                cleanWorkspace(scriptExecution);
            }
        }
    }

    /**
     * Setups the environment (i.e. workspace) in which a script will be executed.
     *
     * @param scriptExecution the script being executed
     * @throws WorkerException indicates an exception in the Java code (not the script)
     */
    protected void setupEnv(ScriptExecution scriptExecution) throws WorkerException {
        // 1. create a workspace
        Path workspace = Path.of(config.getWorkspace(), UUID.randomUUID().toString());
        scriptExecution.setWorkspace(workspace);
        try {
            Files.createDirectories(workspace);
        } catch (IOException e) {
            throw new WorkerException("Cannot create workspace", e);
        }

        // 2. write input to file
        Path inputFile = workspace.resolve("input.json");
        try {
            Files.createFile(inputFile);
            Files.writeString(inputFile, scriptExecution.getScriptExecutionInput().getInput());
        } catch (IOException e) {
            throw new WorkerException("Cannot create input file", e);
        }

        // 3. write script to workdir
        Path scriptFile = workspace.resolve(getScriptName(scriptExecution));
        try {
            String script = getFullScript(scriptExecution);
            Files.createFile(scriptFile);
            Files.writeString(scriptFile, script);
        } catch (IOException e) {
            throw new WorkerException("Cannot create script file", e);
        }
    }

    /**
     * Produces the full script to execute (including any "header" or "footer" code).
     *
     * @param scriptExecution the script being executed
     * @return the full script to execute
     */
    protected abstract String getFullScript(ScriptExecution scriptExecution);

    /**
     * @param scriptExecution the script being executed
     * @return The filename of the script.
     */
    protected abstract String getScriptName(ScriptExecution scriptExecution);

    /**
     * Executes the script.
     *
     * @param scriptExecution the script being executed.
     * @return exit code of the script
     * @throws WorkerException      indicates an exception in the Java code (not the script)
     * @throws InterruptedException when the thread is interrupted when waiting for the script to finish.
     */
    protected abstract int executeScript(ScriptExecution scriptExecution) throws WorkerException, InterruptedException;

    protected Boolean checkOutput(ScriptExecution scriptExecution) {
        return Files.exists(scriptExecution.getWorkspace().resolve("output.json"));
    }

    /**
     * Reads the output produced by the script.
     *
     * @param scriptExecution the script being executed
     * @throws WorkerException indicates an exception in the Java code (not the script)
     */
    protected String readOutput(ScriptExecution scriptExecution) throws WorkerException {
        try {
            return Files.readString(scriptExecution.getWorkspace().resolve("output.json"));
        } catch (IOException e) {
            throw new WorkerException("Cannot read output file", e);
        }
    }

    /**
     * Cleans the environment (i.e. workspace) in which a script was executed.
     * Counterpart of @{link setupEnv}
     *
     * @param scriptExecution the executed script
     */
    protected void cleanWorkspace(ScriptExecution scriptExecution) {
        try {
            FileSystemUtils.deleteRecursively(scriptExecution.getWorkspace());
        } catch (IOException e) {
            logger.warn("Cannot remove workspace", e);
        }
    }

}
