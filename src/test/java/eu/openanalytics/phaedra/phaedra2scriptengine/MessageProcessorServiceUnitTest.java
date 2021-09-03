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
package eu.openanalytics.phaedra.phaedra2scriptengine;

import eu.openanalytics.phaedra.model.v2.ModelMapper;
import eu.openanalytics.phaedra.model.v2.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.model.v2.enumeration.ResponseStatusCode;
import eu.openanalytics.phaedra.phaedra2scriptengine.config.data.Config;
import eu.openanalytics.phaedra.phaedra2scriptengine.service.MessageProcessorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.data.util.Pair;

import java.nio.charset.StandardCharsets;

// Unit Test
public class MessageProcessorServiceUnitTest {

    private final ModelMapper modelMapper = new ModelMapper();

    @Test
    public void basicTest() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var processor = new MessageProcessorService(
            scriptExecution -> new ScriptExecutionOutputDTO("myId", "myOutput", ResponseStatusCode.SUCCESS, "Ok", 0),
            config,
            event -> {
            }, modelMapper);

        Pair<String, Message> response = processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"response_topic_suffix\": \"myTopic\", \"id\": \"myId\", \"queue_timestamp\": 1024}}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertEquals("scriptengine.output.myTopic", response.getFirst());
        Assertions.assertEquals("{\"status_message\":\"Ok\",\"output\":\"myOutput\",\"status_code\":\"SUCCESS\",\"exit_code\":0,\"input_id\":\"myId\"}", new String(response.getSecond().getBody(), StandardCharsets.UTF_8));
    }

    @Test
    public void invalidInput() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var processor = new MessageProcessorService(
            scriptExecution -> new ScriptExecutionOutputDTO("myId", "myOutput", ResponseStatusCode.SUCCESS, "Ok", 0),
            config,
            event -> {
            }, modelMapper);

        Pair<String, Message> response = processor.processMessage(new Message("{\"}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNull(response);
    }

    @Test
    public void invalidInputMissingField() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var processor = new MessageProcessorService(
            scriptExecution -> new ScriptExecutionOutputDTO("myId", "myOutput", ResponseStatusCode.SUCCESS, "Ok", 0),
            config,
            event -> {
            }, modelMapper);

        // missing id
        Assertions.assertNull(processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"response_topic_suffix\": \"myTopic\"}".getBytes(StandardCharsets.UTF_8))));

        // missing script
        Assertions.assertNull(processor.processMessage(new Message("{\"input\": \"myInput\", \"response_topic_suffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8))));

        // missing input
        Assertions.assertNull(processor.processMessage(new Message("{\"script\": \"myScript\", \"response_topic_suffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8))));

        // missing response_topic_suffix
        Assertions.assertNull(processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void exceptionInExecutor() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var processor = new MessageProcessorService(
            scriptExecution -> {
                throw new RuntimeException("oops");
            },
            config,
            event -> {
            }, modelMapper);

        Pair<String, Message> response = processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"response_topic_suffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNull(response);
    }


    @Test
    public void executorReturnsNull() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var processor = new MessageProcessorService(
            scriptExecution -> null,
            config,
            event -> {
            }, modelMapper);

        Pair<String, Message> response = processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"response_topic_suffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNull(response);
    }

    @Test
    public void executorReturnsInvalidResponse() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var processor = new MessageProcessorService(
            scriptExecution -> new ScriptExecutionOutputDTO(null, null, null, null, 0),
            config,
            event -> {
            }, modelMapper);

        Pair<String, Message> response = processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"response_topic_suffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNull(response);
    }

}
