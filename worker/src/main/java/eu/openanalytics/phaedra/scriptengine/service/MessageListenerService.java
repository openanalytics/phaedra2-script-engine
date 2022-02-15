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
package eu.openanalytics.phaedra.scriptengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import eu.openanalytics.phaedra.scriptengine.config.EnvConfig;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.event.ScriptProcessedEvent;
import eu.openanalytics.phaedra.scriptengine.event.ScriptReceivedEvent;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Service
public class MessageListenerService implements ChannelAwareMessageListener {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ApplicationEventPublisher applicationEventPublisher;
    private final EnvConfig config;
    private final IExecutor executor;
    private final HeartbeatSenderService heartbeatSenderService;

    public MessageListenerService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper, ApplicationEventPublisher applicationEventPublisher, EnvConfig config, IExecutor executor, HeartbeatSenderService heartbeatSenderService) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.applicationEventPublisher = applicationEventPublisher;
        this.config = config;
        this.executor = executor;
        this.heartbeatSenderService = heartbeatSenderService;
    }

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        if (ShutdownService.isShuttingDown()) {
            logger.warn("Ignoring message (not acking it) because we are shutting down!");
            return;
        }
        ScriptExecutionInputDTO input = parseMessage(message);
        if (input == null) {
            // 1. still ack the message, otherwise it will eventually be re-queued
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 1. send a notification that we are going to process this input
        heartbeatSenderService.sendAndStartHeartbeats(input);
        // 2. now ack the message
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        Duration timeInQueue = Duration.between(Instant.ofEpochMilli(input.getQueueTimestamp()), Instant.now());
        applicationEventPublisher.publishEvent(new ScriptReceivedEvent(this, input.getId(), timeInQueue));

        try {
            var scriptExecutionOutput = executor.execute(input);
            if (scriptExecutionOutput == null) return;

            var response = constructResponse(scriptExecutionOutput);
            if (response == null) return;

            rabbitTemplate.send(constructResponseRoutingKey(input), response);
            applicationEventPublisher.publishEvent(new ScriptProcessedEvent(this, input.getId()));
        } catch (Exception e) {
            logger.warn("Exception while processing message" + message, e);
        } finally {
            heartbeatSenderService.stopHeartbeats(input);
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
            logger.warn("Cannot construct response message from execution result" + scriptExecutionResult.getInputId(), e);
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
