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
package eu.openanalytics.phaedra.scriptengine.stat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service registering and updating the Micrometer metrics.
 */
@Service
public class Micrometer {

    private final Logger logger = LogManager.getLogger(getClass());

    private final Counter processedScripts;
    private final Timer receiveDelay;

    public Micrometer(MeterRegistry registry, UserPercentageService idleWatcher) {
        processedScripts = registry.counter("phaedra2_scriptengine_worker_processed_scripts");
        receiveDelay = registry.timer("phaedra2_scriptengine_worker_receive_delay");
        registry.gauge("phaedra2_scriptengine_worker_percentage_busy", Tags.empty(), idleWatcher, UserPercentageService::getBusyPercentage);
    }

    @Async
    @EventListener
    public void onScriptProcessedEvent(ScriptProcessedEvent event) {
        logger.debug("Script executed {}, received-delay: {} ms", event.getScriptExecutionId(), event.getTimeInQueue().toMillis());
        processedScripts.increment();
        receiveDelay.record(event.getTimeInQueue());
    }

}
