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
package eu.openanalytics.phaedra.scriptengine.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.scriptengine.config.EnvConfig;
import eu.openanalytics.phaedra.scriptengine.dto.HeartbeatDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import static eu.openanalytics.phaedra.scriptengine.ScriptEngineWorkerApplication.HEARTBEAT_EXCHANGE;

@Service
public class HeartbeatSenderService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final Set<String> executionsInProgress = ConcurrentHashMap.newKeySet();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public HeartbeatSenderService(EnvConfig envConfig, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;

        // send at fixed rate so that the heartbeats are sent at the exact times
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (var executionId : executionsInProgress) {
                    try {
                        sendHeartbeat(executionId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, envConfig.getHeartbeatInterval() * 1000L);

    }

    public void sendAndStartHeartbeats(ScriptExecutionInputDTO input) throws JsonProcessingException {
        sendHeartbeat(input.getId());
        executionsInProgress.add(input.getId());
    }

    public void stopHeartbeats(ScriptExecutionInputDTO input) {
        executionsInProgress.remove(input.getId());
    }

    private void sendHeartbeat(String id) throws JsonProcessingException {
        var msg = new Message(objectMapper.writeValueAsBytes(HeartbeatDTO.builder().scriptExecutionId(id).build()));
        rabbitTemplate.send(HEARTBEAT_EXCHANGE, "heartbeat", msg);
        logger.info("Send heartbeat for {}", id);
    }

}
