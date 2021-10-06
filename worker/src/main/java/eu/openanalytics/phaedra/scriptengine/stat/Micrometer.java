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

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service registering and updating the Micrometer metrics.
 */
@Service
public class Micrometer {

    private final Logger logger = LogManager.getLogger(getClass());

    private final Counter processedScripts;
    private final Timer receiveDelay;
    private final Timer idleTime;
    private final AtomicLong lastMessage = new AtomicLong(System.currentTimeMillis());

    private final MinIdleTime minIdleTimeHelper;

    public Micrometer(MeterRegistry registry) {
        processedScripts = registry.counter("phaedra2_scriptengine_worker_processed_scripts");
        receiveDelay = registry.timer("phaedra2_scriptengine_worker_receive_delay");
        // we use publishPercentiles(0) to add the minimal value to the metrics
        idleTime = Timer.builder("phaedra2_scriptengine_worker_idle_time").publishPercentiles(0).register(registry);

        // register a gauge that computes the minimal idle time
        minIdleTimeHelper = new MinIdleTime(idleTime);
        registry.gauge("phaedra2_scriptengine_worker_idle_time_min", Tags.empty(), minIdleTimeHelper, MinIdleTime::getValue);
    }

    /**
     * A helper class to compute the minimum idle time. When the worker receives no input messages,
     * the minimum value will always be 0. However, this is misleading as it seems that the worker is working very hard in that case,
     * when in fact it's receiving no messages. Therefore, when recording the idle time, the minimum is always 1ms ({@link Micrometer#onScriptReceivedEvent}.
     * We then never output 0ms as the minimum value, but instead output {@link Double.NaN} to indicate that no input messages were received.
     */
    private static class MinIdleTime {
        private final Timer idleTime;

        private MinIdleTime(Timer idleTime) {
            this.idleTime = idleTime;
        }

        public double getValue() {
            var pr = idleTime.percentile(0.0, TimeUnit.MILLISECONDS);
            if (pr < 0.1) {
                return Double.NaN;
            }
            return pr;
        }
    }

    @Async
    @EventListener
    public void onScriptProcessedEvent(ScriptProcessedEvent event) {
        logger.info("Script executed {}", event.getScriptExecutionId());
        processedScripts.increment();
        lastMessage.set(System.currentTimeMillis());
    }

    @Async
    @EventListener
    public void onScriptReceivedEvent(ScriptReceivedEvent event) {
        long lastMessageFinished = lastMessage.get();
        long timeBetweenMessage = Math.max(1, System.currentTimeMillis() - lastMessageFinished);

        logger.info("Script received {}, timeInQueue: {}ms, timeAfterLastInput: {}ms", event.getScriptExecutionId(), event.getTimeInQueue().toMillis(), timeBetweenMessage);
        receiveDelay.record(event.getTimeInQueue());
        idleTime.record(Duration.ofMillis(timeBetweenMessage));
    }

}
