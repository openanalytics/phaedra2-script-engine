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
package eu.openanalytics.phaedra.phaedra2scriptengine.stat;

import eu.openanalytics.phaedra.phaedra2scriptengine.service.MessagePollerService;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

@Component
public class IdleWatcher {

    private final CircularFifoQueue<Boolean> isBusyPerSecond = new CircularFifoQueue<>(120);

    public IdleWatcher(MessagePollerService messagePollerService) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                isBusyPerSecond.add(messagePollerService.isBusy());
            }
        }, 0, 500);
    }

    public double getBusyPercentage() {
        var current = isBusyPerSecond.stream().collect(Collectors.toUnmodifiableList());
        var busyCount = current.stream().filter((el) -> el).count();
        var totalCount = current.size();
        return (double) busyCount / (double) totalCount;
    }

}
