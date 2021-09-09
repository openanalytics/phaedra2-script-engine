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
package eu.openanalytics.phaedra.scriptengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.scriptengine.config.EnvConfig;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutor;
import eu.openanalytics.phaedra.scriptengine.stat.ScriptProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * Processes any incoming messages and executes the requested script.
 */
@Service
public class MessageProcessorService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final EnvConfig config;
    private final IExecutor executor;

    private final ApplicationEventPublisher applicationEventPublisher;

    public MessageProcessorService(IExecutor executor, EnvConfig config, ApplicationEventPublisher applicationEventPublisher) {
        this.executor = executor;
        this.config = config;
        this.applicationEventPublisher = applicationEventPublisher;
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
        ScriptExecutionInputDTO input = parseMessage(message);
        if (input == null) return null;
        logger.debug("Received a valid input message.");

        Duration timeInQueue = Duration.between(Instant.ofEpochMilli(input.getQueueTimestamp()), Instant.now());

        try {
            var scriptExecutionOutput = executor.execute(input);
            if (scriptExecutionOutput == null) return null;

            var response = constructResponse(scriptExecutionOutput);
            if (response == null) return null;

            applicationEventPublisher.publishEvent(new ScriptProcessedEvent(this, input.getId(), timeInQueue));

            return Pair.of(constructResponseRoutingKey(input), response);
        } catch (Exception e) {
            logger.warn("Exception while processing message" + message, e);
            return null;
        }
    }

    /**
     * Converts the incoming message to a {@link ScriptExecutionInputDTO}
     *
     * @param message the message to parse
     * @return the parsed message or null when it is invalid
     */
    private ScriptExecutionInputDTO parseMessage(Message message) {
        try {
            return objectMapper.readValue(message.getBody(), ScriptExecutionInputDTO.class);
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
    private Message constructResponse(ScriptExecutionOutputDTO scriptExecutionResult) {
        try {
            return new Message(objectMapper.writeValueAsBytes(scriptExecutionResult));
        } catch (Exception e) {
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
    private String constructResponseRoutingKey(ScriptExecutionInputDTO input) {
        String res = config.getOutputRoutingKeyPrefix() + input.getResponseTopicSuffix();
        logger.debug("Sending response to {}", res);
        return res;
    }

}
