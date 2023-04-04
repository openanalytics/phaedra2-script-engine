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

import static eu.openanalytics.phaedra.scriptengine.config.KafkaConfig.EVENT_REQUEST_SCRIPT_EXECUTION;
import static eu.openanalytics.phaedra.scriptengine.config.KafkaConfig.EVENT_SCRIPT_EXECUTION_UPDATE;
import static eu.openanalytics.phaedra.scriptengine.config.KafkaConfig.TOPIC_SCRIPTENGINE;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.phaedra.scriptengine.config.EnvConfig;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutor;

@Service
public class MessageHandlingService {
	
	private final KafkaTemplate<String, Object> kafkaTemplate;
	
	private final EnvConfig envConfig;
    private final ObjectMapper objectMapper;
    private final IExecutor executor;
    private final HeartbeatSenderService heartbeatSenderService;
    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MessageHandlingService(KafkaTemplate<String, Object> kafkaTemplate, EnvConfig envConfig, ObjectMapper objectMapper, IExecutor executor, HeartbeatSenderService heartbeatSenderService) {
    	this.kafkaTemplate = kafkaTemplate;
    	this.envConfig = envConfig;
        this.objectMapper = objectMapper;
        this.executor = executor;
        this.heartbeatSenderService = heartbeatSenderService;
    }

    @Bean
    public RecordFilterStrategy<String, Object> scriptExecutionRequestFilter() {
        return rec -> !(rec.key().equalsIgnoreCase(EVENT_REQUEST_SCRIPT_EXECUTION));
    }
    
    @KafkaListener(topics = TOPIC_SCRIPTENGINE, filter = "scriptExecutionRequestFilter")
    public void onScriptExecutionRequest(String message) {
    	ScriptExecutionInputDTO input = null;
    	try {
            input = objectMapper.readValue(message, ScriptExecutionInputDTO.class);
        } catch (IOException e) {
            logger.warn("Ignoring invalid input message: " + message, e);
            return;
        }
    	
    	boolean matchesLanguage = envConfig.getLanguage().equalsIgnoreCase(input.getLanguage());
    	if (!matchesLanguage) return; // Let another consumer group handle this message.
    	
    	try {
    		heartbeatSenderService.sendAndStartHeartbeats(input);
    		
    		logger.info("Processing script execution request: " + input.getId());
    		ScriptExecutionOutputDTO scriptExecutionOutput = executor.execute(input);
            if (scriptExecutionOutput != null) {
            	kafkaTemplate.send(TOPIC_SCRIPTENGINE, EVENT_SCRIPT_EXECUTION_UPDATE, scriptExecutionOutput);
            }
        } catch (Exception e) {
            logger.warn("Exception while processing message " + message, e);
        } finally {
            heartbeatSenderService.stopHeartbeats(input);
        }
    }
}
