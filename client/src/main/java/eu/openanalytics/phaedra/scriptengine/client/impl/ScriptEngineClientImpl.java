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
package eu.openanalytics.phaedra.scriptengine.client.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.scriptengine.client.ScriptEngineClient;
import eu.openanalytics.phaedra.scriptengine.client.config.ScriptEngineClientConfiguration;
import eu.openanalytics.phaedra.scriptengine.client.model.ScriptExecution;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptEngineClientImpl implements MessageListener, ScriptEngineClient {

    private final ScriptEngineClientConfiguration clientConfig;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConcurrentHashMap<String, ScriptExecution> executionsInProgress = new ConcurrentHashMap<>();

    public ScriptEngineClientImpl(ScriptEngineClientConfiguration clientConfig, RabbitTemplate rabbitTemplate) {
        this.clientConfig = clientConfig;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ScriptExecution newScriptExecution(String targetName, String script, String input) {
        var target = clientConfig.getTargetRuntime(targetName);

        return new ScriptExecution(
            target,
            script,
            input,
            clientConfig.getClientName());
    }

    @Override
    public void execute(ScriptExecution scriptExecution) throws JsonProcessingException {
        // first save execution (before sending the message, otherwise it may not be saved before the response is received)
        executionsInProgress.put(scriptExecution.getScriptExecutionInput().getId(), scriptExecution);

        // send message
        rabbitTemplate.send(
            "scriptengine_input",
            scriptExecution.getTargetRuntime().getRoutingKey(),
            new Message(objectMapper.writeValueAsBytes(scriptExecution.getScriptExecutionInput()))
        );
    }

    @Override
    public void onMessage(Message message) {
        try {
            ScriptExecutionOutputDTO output = objectMapper.readValue(message.getBody(), ScriptExecutionOutputDTO.class);

            var scriptExecution = executionsInProgress.get(output.getInputId());
            if (scriptExecution != null) {
                scriptExecution.getOutput().complete(output);
                executionsInProgress.remove(output.getInputId());
            } else {
                logger.warn("No execution found, for output id " + output.getInputId());
            }
        } catch (IOException e) {
            logger.warn("Exception during handling of incoming output message", e);
        }
    }

}
