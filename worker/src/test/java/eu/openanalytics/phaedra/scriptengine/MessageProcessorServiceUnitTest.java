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

import eu.openanalytics.phaedra.scriptengine.config.EnvConfig;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.service.MessageProcessorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.data.util.Pair;

import java.nio.charset.StandardCharsets;

// Unit Test
public class MessageProcessorServiceUnitTest {

    @Test
    public void basicTest() throws InterruptedException {
        var processor = new MessageProcessorService(
            scriptExecution -> new ScriptExecutionOutputDTO(scriptExecution.getId(), "myOutput", ResponseStatusCode.SUCCESS, "Ok", 0),
            new EnvConfig(),
            event -> {
            });

        Pair<String, Message> response = processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"responseTopicSuffix\": \"myTopic\", \"id\": \"myId\", \"queueTimestamp\": 1024}}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertEquals("scriptengine.output.myTopic", response.getFirst());
        Assertions.assertEquals("{\"inputId\":\"myId\",\"output\":\"myOutput\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"exitCode\":0}", new String(response.getSecond().getBody(), StandardCharsets.UTF_8));
    }

    @Test
    public void invalidInput() throws InterruptedException {
        var processor = new MessageProcessorService(
            scriptExecution -> new ScriptExecutionOutputDTO(scriptExecution.getId(), "myOutput", ResponseStatusCode.SUCCESS, "Ok", 0),
            new EnvConfig(),
            event -> {
            });

        Pair<String, Message> response = processor.processMessage(new Message("{\"}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNull(response);
    }

    @Test
    public void invalidInputMissingField() throws InterruptedException {
        var processor = new MessageProcessorService(
            scriptExecution -> new ScriptExecutionOutputDTO(scriptExecution.getId(), "myOutput", ResponseStatusCode.SUCCESS, "Ok", 0),
            new EnvConfig(),
            event -> {
            });

        // missing id
        Assertions.assertNull(processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"responseTopicSuffix\": \"myTopic\"}".getBytes(StandardCharsets.UTF_8))));

        // missing script
        Assertions.assertNull(processor.processMessage(new Message("{\"input\": \"myInput\", \"responseTopicSuffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8))));

        // missing input
        Assertions.assertNull(processor.processMessage(new Message("{\"script\": \"myScript\", \"responseTopicSuffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8))));

        // missing responseTopicSuffix
        Assertions.assertNull(processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void exceptionInExecutor() throws InterruptedException {
        var processor = new MessageProcessorService(
            scriptExecution -> {
                throw new RuntimeException("oops");
            },
            new EnvConfig(),
            event -> {
            });

        Pair<String, Message> response = processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"responseTopicSuffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNull(response);
    }


    @Test
    public void executorReturnsNull() throws InterruptedException {
        var processor = new MessageProcessorService(
            scriptExecution -> null,
            new EnvConfig(),
            event -> {
            });

        Pair<String, Message> response = processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"responseTopicSuffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNull(response);
    }

    @Test
    public void executorReturnsInvalidResponse() throws InterruptedException {
        var processor = new MessageProcessorService(
            scriptExecution -> new ScriptExecutionOutputDTO(null, null, null, null, 0),
            new EnvConfig(),
            event -> {
            });

        Pair<String, Message> response = processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"responseTopicSuffix\": \"myTopic\", \"id\": \"myId\", \"queueTimestamp\": 1024}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNull(response);
    }

}
