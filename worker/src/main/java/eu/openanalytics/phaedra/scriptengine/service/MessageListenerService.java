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
package eu.openanalytics.phaedra.scriptengine.service;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

/**
 * Service that listens for messages on the queue and passes them to the {@link MessageProcessorService}.
 * Messages are handled in a synchronous, blocking way in order to ensure that only one message is being processed by this service at a time.
 */
@Service
public class MessageListenerService implements MessageListener {
    private final RabbitTemplate rabbitTemplate;
    private final MessageProcessorService messageProcessorService;

    public MessageListenerService(RabbitTemplate rabbitTemplate, MessageProcessorService messageProcessorService) {
        this.rabbitTemplate = rabbitTemplate;
        this.messageProcessorService = messageProcessorService;
    }

    @Override
    public void onMessage(Message message) {
        try {
            Pair<String, Message> response = messageProcessorService.processMessage(message);
            if (response == null) {
                return; // TODO
            }
            rabbitTemplate.send(response.getFirst(), response.getSecond());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
