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
package eu.openanalytics.phaedra.scriptengine.client.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import eu.openanalytics.phaedra.scriptengine.client.impl.ScriptEngineClientImpl;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.ConnectionFactoryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;

@Configuration
@ConditionalOnProperty(value = "phaedra2.scriptengine.client.enabled", havingValue = "true", matchIfMissing = true)
public class ScriptEngineClientAutoConfiguration {

    @Autowired
    private ScriptEngineClientConfiguration clientConfig;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @PostConstruct
    public void init() {
        amqpAdmin.declareQueue(new Queue(clientConfig.getResponseQueueName(), true, false, false));
        amqpAdmin.declareBinding(new Binding(clientConfig.getResponseQueueName(), Binding.DestinationType.QUEUE,
                "scriptengine_output", "scriptengine.output." + clientConfig.getClientName(), Map.of()));
    }

    @Bean
    public ScriptEngineClientImpl scriptEngineClient(RabbitTemplate rabbitTemplate, ScriptEngineClientConfiguration clientConfig) {
        return new ScriptEngineClientImpl(clientConfig, rabbitTemplate);
    }

    @Bean
    public DirectMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory, RabbitTemplate rabbitTemplate, ScriptEngineClientConfiguration clientConfig) {
        DirectMessageListenerContainer container = new DirectMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addQueueNames(clientConfig.getResponseQueueName());
        container.setMessageListener(scriptEngineClient(rabbitTemplate, clientConfig));
        container.setPrefetchCount(250);
        container.setConsumersPerQueue(8);
        return container;
    }

    @Bean
    public ConnectionFactoryCustomizer connectionFactoryCustomizer() {
        return factory -> {
            var threadFactory = new ThreadFactoryBuilder().setNameFormat("rabbitmq-con-%s").build();
            factory.setThreadFactory(threadFactory);
        };
    }

}
