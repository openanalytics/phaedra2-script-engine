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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

/**
 * Service that polls for messages on the queue and passes them to the {@link MessageProcessorService}.
 * Messages are handled in a synchronous, blocking way in order to ensure that only one message is being processed by this service at a time.
 */
@Service
public class MessagePollerService {

    public final static int POLLING_TIMEOUT = 10000;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile boolean enabled = true;

    private volatile boolean isBusy = false;

    public MessagePollerService(RabbitTemplate rabbitTemplate, MessageProcessorService messageProcessorService) {
        Thread daemonThread = new Thread(() -> {
            while (true) {
                if (!enabled) {
                    try {
                        Thread.sleep(POLLING_TIMEOUT);
                    } catch (InterruptedException e) {
                        break;
                    }
                    continue;
                }
                isBusy = false;
                Message message = rabbitTemplate.receive(POLLING_TIMEOUT);
                if (message == null) {
                    logger.info("Received no message");
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    continue;
                }
                isBusy = true;

                try {
                    Pair<String, Message> response = messageProcessorService.processMessage(message);
                    if (response == null) continue;
                    rabbitTemplate.send(response.getFirst(), response.getSecond());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        daemonThread.setDaemon(true);
        daemonThread.start();
    }

    public void stop() {
        enabled = false;
    }

    public void start() {
        enabled = true;
    }

    /**
     * @return whether this worker is currently processing a script
     */
    public Boolean isBusy() {
        return isBusy;
    }
}
