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

import eu.openanalytics.phaedra.scriptengine.service.MessagePollerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ScriptEngineWorkerApplication.class)
@TestPropertySource(locations = "classpath:application-integration-test.properties")
public class MainIntegrationTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private MessagePollerService messagePollerService;

    @Container
    public static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management")
        .withAdminPassword(null);

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
    }

    @Test
    public void mainIntegrationTest() throws ExecutionException, InterruptedException, TimeoutException {
        // create output exchange, queue and binding
        amqpAdmin.declareExchange(new TopicExchange("scriptengine_output", true, false));
        amqpAdmin.declareQueue(new Queue("scriptengine_output", true, false, false));
        amqpAdmin.declareBinding(new Binding("scriptengine_output", Binding.DestinationType.QUEUE, "scriptengine_output", "scriptengine.output.calculationService", Map.of()));

        var rabbitTemplate = new RabbitTemplate(connectionFactory);

        // start listening for responses
        var receivedResponse = new AssertAsync(() -> {
            Message response = rabbitTemplate.receive("scriptengine_output", 3000);
            Assertions.assertNotNull(response);
            Assertions.assertEquals("{\"inputId\":\"myId\",\"output\":\"{\\\"output\\\":3}\\n\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"exitCode\":0}",
                new String(response.getBody(), StandardCharsets.UTF_8));
        });

        // send message
        rabbitTemplate.send("scriptengine_input", "scriptengine.input.fast-lane.r.v1", new Message("{\"script\": \"output <- input$a + input$b\", \"input\": \"{\\\"a\\\": 1,\\\"b\\\":2}\", \"responseTopicSuffix\": \"calculationService\", \"id\": \"myId\", \"queueTimestamp\": 1024}}".getBytes(StandardCharsets.UTF_8)));

        // wait for response or timeout
        receivedResponse.assertCalled(5000);
    }

    @Test
    public void testThatOutputQueueIsDurable() throws ExecutionException, InterruptedException, TimeoutException {
        // create output exchange, queue and binding
        amqpAdmin.declareExchange(new TopicExchange("scriptengine_output", true, false));
        amqpAdmin.declareQueue(new Queue("scriptengine_output", true, false, false));
        amqpAdmin.declareBinding(new Binding("scriptengine_output", Binding.DestinationType.QUEUE, "scriptengine_output", "scriptengine.output.calculationService", Map.of()));

        var rabbitTemplate = new RabbitTemplate(connectionFactory);

        // send message
        rabbitTemplate.send("scriptengine_input", "scriptengine.input.fast-lane.r.v1", new Message("{\"script\": \"output <- input$a + input$b\", \"input\": \"{\\\"a\\\": 1,\\\"b\\\":2}\", \"responseTopicSuffix\": \"calculationService\", \"id\": \"myId\", \"queueTimestamp\": 1024}}".getBytes(StandardCharsets.UTF_8)));

        // sleep a bit
        Thread.sleep(5000);

        // now receive the response and assart that it is still there
        Message response = rabbitTemplate.receive("scriptengine_output", 3000);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("{\"inputId\":\"myId\",\"output\":\"{\\\"output\\\":3}\\n\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"exitCode\":0}",
            new String(response.getBody(), StandardCharsets.UTF_8));
    }

    @Test
    public void testThatInputQueueIsDurable() throws ExecutionException, InterruptedException, TimeoutException {
        // create output exchange, queue and binding
        amqpAdmin.declareExchange(new TopicExchange("scriptengine_output", true, false));
        amqpAdmin.declareQueue(new Queue("scriptengine_output", true, false, false));
        amqpAdmin.declareBinding(new Binding("scriptengine_output", Binding.DestinationType.QUEUE, "scriptengine_output", "scriptengine.output.calculationService", Map.of()));

        var rabbitTemplate = new RabbitTemplate(connectionFactory);

        // stop poller
        messagePollerService.stop();

        // sleep for current interval to stop
        Thread.sleep(MessagePollerService.POLLING_TIMEOUT);

        // send message
        rabbitTemplate.send("scriptengine_input", "scriptengine.input.fast-lane.r.v1", new Message("{\"script\": \"output <- input$a + input$b\", \"input\": \"{\\\"a\\\": 1,\\\"b\\\":2}\", \"responseTopicSuffix\": \"calculationService\", \"id\": \"myId\", \"queueTimestamp\": 1024}".getBytes(StandardCharsets.UTF_8)));

        // wait until poller restarts
        Thread.sleep(10000);

        // start poller again
        messagePollerService.start();

        // now receive the response and assert that it is still there
        Message response = rabbitTemplate.receive("scriptengine_output", 10000);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("{\"inputId\":\"myId\",\"output\":\"{\\\"output\\\":3}\\n\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"exitCode\":0}",
            new String(response.getBody(), StandardCharsets.UTF_8));
    }

}

