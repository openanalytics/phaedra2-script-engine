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

import static eu.openanalytics.phaedra.scriptengine.config.KafkaConfig.EVENT_SCRIPT_EXECUTION_HEARTBEAT;
import static eu.openanalytics.phaedra.scriptengine.config.KafkaConfig.TOPIC_SCRIPTENGINE;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.openanalytics.phaedra.scriptengine.config.EnvConfig;
import eu.openanalytics.phaedra.scriptengine.dto.HeartbeatDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;

@Service
public class HeartbeatSenderService {

    private final Set<String> executionsInProgress = ConcurrentHashMap.newKeySet();
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    public HeartbeatSenderService(KafkaTemplate<String, Object> kafkaTemplate, EnvConfig envConfig) {
    	this.kafkaTemplate = kafkaTemplate;

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
    	HeartbeatDTO heartbeat = HeartbeatDTO.builder().scriptExecutionId(id).build();
    	kafkaTemplate.send(TOPIC_SCRIPTENGINE, EVENT_SCRIPT_EXECUTION_HEARTBEAT, heartbeat);
        logger.info("Sent heartbeat for {}", id);
    }

}
