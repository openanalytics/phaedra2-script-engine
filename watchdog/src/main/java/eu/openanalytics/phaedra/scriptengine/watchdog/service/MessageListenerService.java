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
package eu.openanalytics.phaedra.scriptengine.watchdog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.scriptengine.dto.HeartbeatDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.watchdog.repository.ScriptExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Service;

import static eu.openanalytics.phaedra.scriptengine.watchdog.WatchdogApplication.HEARTBEAT_QUEUE_NAME;
import static eu.openanalytics.phaedra.scriptengine.watchdog.WatchdogApplication.INPUT_QUEUE_NAME;
import static eu.openanalytics.phaedra.scriptengine.watchdog.WatchdogApplication.OUTPUT_QUEUE_NAME;

@Service
public class MessageListenerService implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScriptExecutionRepository scriptExecutionRepository;

    public MessageListenerService(ScriptExecutionRepository scriptExecutionRepository) {
        this.scriptExecutionRepository = scriptExecutionRepository;
    }

    @Override
    public void onMessage(Message message) {
        try {
            switch (message.getMessageProperties().getConsumerQueue()) {
                case INPUT_QUEUE_NAME -> {
                    var routingKey = message.getMessageProperties().getReceivedRoutingKey();
                    var input = objectMapper.readValue(message.getBody(), ScriptExecutionInputDTO.class);
                    onInput(input, routingKey);
                }
                case OUTPUT_QUEUE_NAME -> {
                    var output = objectMapper.readValue(message.getBody(), ScriptExecutionOutputDTO.class);
                    onOutput(output);
                }
                case HEARTBEAT_QUEUE_NAME -> {
                    var heartbeat = objectMapper.readValue(message.getBody(), HeartbeatDTO.class);
                    onHeartbeat(heartbeat);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void onHeartbeat(HeartbeatDTO heartbeat) {
        logger.debug("Heartbeat: {}", heartbeat.getScriptExecutionId());
        scriptExecutionRepository.updateScriptExecution(heartbeat);
    }

    public void onInput(ScriptExecutionInputDTO input, String routingKey) {
        logger.debug("Input:      {}", input.getId());
        scriptExecutionRepository.createScriptExecution(input, routingKey);
    }

    public void onOutput(ScriptExecutionOutputDTO output) {
        logger.debug("Output      {}", output.getInputId());
        scriptExecutionRepository.stopScriptExecution(output);
    }

}
