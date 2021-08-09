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

import eu.openanalytics.phaedra.phaedra2scriptengine.config.data.Config;
import eu.openanalytics.phaedra.phaedra2scriptengine.config.data.EnvConfig;
import eu.openanalytics.phaedra.phaedra2scriptengine.service.executor.IExecutor;
import eu.openanalytics.phaedra.phaedra2scriptengine.service.executor.RExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;

@SpringBootApplication
public class ScriptEngineWorkerApplication {

    private final ConnectionFactory connectionFactory;
    private final EnvConfig envConfig;
    private final Config config;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        SpringApplication.run(ScriptEngineWorkerApplication.class, args);
    }

    private final String inputQueueName;
    private final String outputExchangeName = "scriptengine_output";

    public ScriptEngineWorkerApplication(AmqpAdmin amqpAdmin, ConnectionFactory connectionFactory, EnvConfig envConfig, Config config) {
        this.connectionFactory = connectionFactory;
        this.envConfig = envConfig;
        this.config = config;

        inputQueueName = String.format("scriptengine.input.%s.%s.%s", envConfig.getPoolName(), envConfig.getLanguage(), envConfig.getVersion());

        // input exchange and queues
        String inputExchangeName = "scriptinegine_input";
        amqpAdmin.declareExchange(new DirectExchange(inputExchangeName, true, false));
        amqpAdmin.declareQueue(new Queue(inputQueueName, true, false, false));
        amqpAdmin.declareBinding(new Binding(inputQueueName, Binding.DestinationType.QUEUE, inputExchangeName, inputQueueName, Map.of()));

        // output exchange (-> no queues)
        amqpAdmin.declareExchange(new TopicExchange(outputExchangeName, true, false));

        logger.info("Using {} as name for the input exchange", inputExchangeName);
        logger.info("Using {} as name for the input queue", inputQueueName);
        logger.info("Using {} as routing key for the input queue", inputQueueName);
        logger.info("Using {} as name for the output exchange", outputExchangeName);
        logger.info("Using {} as routing key prefix for the output exchange", config.getOutputRoutingKeyPrefix());
    }


    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setDefaultReceiveQueue(inputQueueName);
        template.setExchange(outputExchangeName);
        return template;
    }

    @Bean
    public IExecutor scriptExecutor() {
        if (envConfig.getLanguage().equalsIgnoreCase("r")) {
            return new RExecutor(config);
        } else {
            throw new IllegalArgumentException(String.format("Unsupported language found %s", envConfig.getLanguage()));
        }
    }

}
