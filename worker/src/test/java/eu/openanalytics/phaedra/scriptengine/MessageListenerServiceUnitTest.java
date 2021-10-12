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
package eu.openanalytics.phaedra.scriptengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import eu.openanalytics.phaedra.scriptengine.config.EnvConfig;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.service.HeartbeatSenderService;
import eu.openanalytics.phaedra.scriptengine.service.MessageListenerService;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

// Unit Test
public class MessageListenerServiceUnitTest {

    RabbitTemplate rabbiTemplate = mock(RabbitTemplate.class);
    Channel channel = mock(Channel.class);
    HeartbeatSenderService heartbeatSenderService = mock(HeartbeatSenderService.class);

    @Test
    public void basicTest() throws Exception {
        var processor = new MessageListenerService(
            rabbiTemplate,
            new ObjectMapper(),
            event -> {
            },
            new EnvConfig(),
            scriptExecution -> new ScriptExecutionOutputDTO(scriptExecution.getId(), "myOutput", ResponseStatusCode.SUCCESS, "Ok", 0),
            heartbeatSenderService
        );

        processor.onMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"responseTopicSuffix\": \"myTopic\", \"id\": \"myId\", \"queueTimestamp\": 1024}}".getBytes(StandardCharsets.UTF_8)), channel);

        verify(channel).basicAck(0, false);
        var input = new ScriptExecutionInputDTO("myId", "myScript", "myInput", "myTopic", 1024L);
        verify(heartbeatSenderService).sendAndStartHeartbeats(input);
        verify(heartbeatSenderService).stopHeartbeats(input);
        verify(rabbiTemplate).send("scriptengine.output.myTopic", new Message("{\"inputId\":\"myId\",\"output\":\"myOutput\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"exitCode\":0}".getBytes(StandardCharsets.UTF_8)));

        verifyNoMoreInteractions(rabbiTemplate, channel, heartbeatSenderService);
    }

    @Test
    public void invalidInput() throws Exception {
        var processor = new MessageListenerService(
            rabbiTemplate,
            new ObjectMapper(),
            event -> {
            },
            new EnvConfig(),
            scriptExecution -> new ScriptExecutionOutputDTO(scriptExecution.getId(), "myOutput", ResponseStatusCode.SUCCESS, "Ok", 0),
            heartbeatSenderService
        );

        processor.onMessage(new Message("{\"}".getBytes(StandardCharsets.UTF_8)), channel);
        verify(channel).basicAck(0, false);

        // the message should not be further processed
        verifyNoMoreInteractions(rabbiTemplate, channel, heartbeatSenderService);
    }

    @Test
    public void invalidInputMissingField() throws Exception {
        var processor = new MessageListenerService(
            rabbiTemplate,
            new ObjectMapper(),
            event -> {
            },
            new EnvConfig(),
            scriptExecution -> new ScriptExecutionOutputDTO(scriptExecution.getId(), "myOutput", ResponseStatusCode.SUCCESS, "Ok", 0),
            heartbeatSenderService
        );

        // missing id
        processor.onMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"responseTopicSuffix\": \"myTopic\"}".getBytes(StandardCharsets.UTF_8)), channel);

        // missing script
        processor.onMessage(new Message("{\"input\": \"myInput\", \"responseTopicSuffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8)), channel);

        // missing input
        processor.onMessage(new Message("{\"script\": \"myScript\", \"responseTopicSuffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8)), channel);

        // missing responseTopicSuffix
        processor.onMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8)), channel);

        verify(channel, times(4)).basicAck(0, false);

        // the messages should not be further processed
        verifyNoMoreInteractions(rabbiTemplate, channel, heartbeatSenderService);
    }

    @Test
    public void exceptionInExecutor() throws Exception {
        var processor = new MessageListenerService(
            rabbiTemplate,
            new ObjectMapper(),
            event -> {
            },
            new EnvConfig(),
            scriptExecution -> {
                throw new RuntimeException("oops");
            },
            heartbeatSenderService
        );

        processor.onMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"responseTopicSuffix\": \"myTopic\", \"id\": \"myId\", \"queueTimestamp\": 1024}}".getBytes(StandardCharsets.UTF_8)), channel);

        verify(channel).basicAck(0, false);
        var input = new ScriptExecutionInputDTO("myId", "myScript", "myInput", "myTopic", 1024L);
        verify(heartbeatSenderService).sendAndStartHeartbeats(input);
        verify(heartbeatSenderService).stopHeartbeats(input);

        // the message should not be further processed
        verifyNoMoreInteractions(rabbiTemplate, channel, heartbeatSenderService);
    }


    @Test
    public void executorReturnsNull() throws Exception {
        var processor = new MessageListenerService(
            rabbiTemplate,
            new ObjectMapper(),
            event -> {
            },
            new EnvConfig(),
            scriptExecution -> null,
            heartbeatSenderService
        );

        processor.onMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"responseTopicSuffix\": \"myTopic\", \"id\": \"myId\", \"queueTimestamp\": 1024}}".getBytes(StandardCharsets.UTF_8)), channel);

        verify(channel).basicAck(0, false);
        var input = new ScriptExecutionInputDTO("myId", "myScript", "myInput", "myTopic", 1024L);
        verify(heartbeatSenderService).sendAndStartHeartbeats(input);
        verify(heartbeatSenderService).stopHeartbeats(input);
        verifyNoMoreInteractions(rabbiTemplate, channel, heartbeatSenderService);
    }

    @Test
    public void executorReturnsInvalidResponse() throws IOException {
        var processor = new MessageListenerService(
            rabbiTemplate,
            new ObjectMapper(),
            event -> {
            },
            new EnvConfig(),
            scriptExecution -> new ScriptExecutionOutputDTO(null, null, null, null, 0),
            heartbeatSenderService
        );

        processor.onMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"responseTopicSuffix\": \"myTopic\", \"id\": \"myId\", \"queueTimestamp\": 1024}".getBytes(StandardCharsets.UTF_8)), channel);
        verify(channel).basicAck(0, false);
        var input = new ScriptExecutionInputDTO("myId", "myScript", "myInput", "myTopic", 1024L);
        verify(heartbeatSenderService).sendAndStartHeartbeats(input);
        verify(heartbeatSenderService).stopHeartbeats(input);
        verifyNoMoreInteractions(rabbiTemplate, channel, heartbeatSenderService);
    }

    @Test
    public void executorReturnsUnserializableObject() throws IOException {
        var res = new ScriptExecutionOutputDTO("myId", "", ResponseStatusCode.SUCCESS, "Ok", 0) {
            @Override
            public @NonNull String getStatusMessage() {
                throw new RuntimeException("Break JSON serialization");
            }
        };
        var processor = new MessageListenerService(
            rabbiTemplate,
            new ObjectMapper(),
            event -> {
            },
            new EnvConfig(),
            scriptExecution -> res,
            heartbeatSenderService
        );

        processor.onMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"responseTopicSuffix\": \"myTopic\", \"id\": \"myId\", \"queueTimestamp\": 1024}".getBytes(StandardCharsets.UTF_8)), channel);
        verify(channel).basicAck(0, false);
        var input = new ScriptExecutionInputDTO("myId", "myScript", "myInput", "myTopic", 1024L);
        verify(heartbeatSenderService).sendAndStartHeartbeats(input);
        verify(heartbeatSenderService).stopHeartbeats(input);
        verifyNoMoreInteractions(rabbiTemplate, channel, heartbeatSenderService);
    }

}
