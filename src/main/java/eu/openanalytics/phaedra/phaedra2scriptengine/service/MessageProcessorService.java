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
package eu.openanalytics.phaedra.phaedra2scriptengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.phaedra2scriptengine.config.data.Config;
import eu.openanalytics.phaedra.phaedra2scriptengine.model.runtime.ScriptExecution;
import eu.openanalytics.phaedra.phaedra2scriptengine.model.runtime.ScriptExecutionInput;
import eu.openanalytics.phaedra.phaedra2scriptengine.model.runtime.ScriptExecutionOutput;
import eu.openanalytics.phaedra.phaedra2scriptengine.service.executor.IExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;

/**
 * Processes any incoming messages and executes the requested script.
 */
@Service
public class MessageProcessorService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Config config;
    private final IExecutor executor;

    public MessageProcessorService(IExecutor executor, Config config) {
        this.executor = executor;
        this.config = config;
    }

    /**
     * Processes the incoming message and executes the requested script.
     *
     * @param message the message to process
     * @return the routing key to which the response must be send + the actual response. This is null when the message was invalid or
     * an unhandled error occurred.
     * @throws InterruptedException when the thread is interrupted when waiting for the script to finish.
     */
    public Pair<String, Message> processMessage(Message message) throws InterruptedException {
        // received a message -> process it
        ScriptExecutionInput input = parseMessage(message);
        if (input == null) return null;
        logger.debug("Received a valid input message.");

        var scriptExecution = new ScriptExecution(input);
        var scriptExecutionOutput = executor.execute(scriptExecution);
        var response = constructResponse(scriptExecutionOutput);

        if (response == null) return null;

        return Pair.of(constructResponseRoutingKey(input), response);
    }

    /**
     * Converts the incoming message to a {@link ScriptExecutionInput}
     *
     * @param message the message to parse
     * @return the parsed message or null when it is invalid
     */
    private ScriptExecutionInput parseMessage(Message message) {
        try {
            return objectMapper.readValue(message.getBody(), ScriptExecutionInput.class);
        } catch (IOException e) {
            logger.warn("Received an invalid input message " + message, e);
            return null;
        }
    }

    /**
     * Constructs the response message.
     *
     * @param scriptExecutionResult the output
     * @return the response message
     */
    private Message constructResponse(ScriptExecutionOutput scriptExecutionResult) {
        try {
            return new Message(
                objectMapper.writeValueAsBytes(new HashMap<>() {{
                    put("input_id", scriptExecutionResult.getScriptExecutionInput().getId());
                    put("status_code", scriptExecutionResult.getStatusCode());
                    put("status_message", scriptExecutionResult.getStatusMessage());
                    put("exit_code", scriptExecutionResult.getExitCode());
                    put("output", scriptExecutionResult.getOutput());
                }})
            );
        } catch (IOException e) {
            logger.warn("Cannot construct response message from execution result" + scriptExecutionResult, e);
            return null;
        }
    }

    /**
     * Constructs the routing key to which the response must be sent.
     *
     * @param input the input for which a response will be sent
     * @return the routing key
     */
    private String constructResponseRoutingKey(ScriptExecutionInput input) {
        String res = config.getOutputRoutingKeyPrefix() + input.getResponseTopicSuffix();
        logger.debug("Sending response to {}", res);
        return res;
    }

}
