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
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Service that calculated the "usage-percentage" of this worker.
 * This is the percentage of the time this worker is busy with processing scripts.
 * This is calculated as a sliding average of the last 60 seconds.
 *
 * There are many ways to keep track of busy/idle time of the worker. For example, you could start/stop a stopwatch
 * every time the worker starts/stops processing a script. However, calculating the "usage-percentage" of the last minute
 * using these stopwatches is not straightforward. E.g. you have to take into account the current state of the worker,
 * cases where the stopwatch is still running etc.
 * Therefore, a different approach is used:
 * - this service contains a {@link Timer} that is executed every 0.5s
 * - the timer asks the {@link MessagePollerService} whethter it is busy and adds this boolean result to the isBusyPerSecond buffer
 * - isBusyPerSecond is a {@link CircularFifoQueue<Boolean>}, it only keeps the last 120 values added to this buffer
 * - therefore this buffer keeps a history of the last minute whether it was busy.
 * - the @{link getBusyPercentage} can then easily calculate the average usage in the last minute.
 */
@Service
public class UserPercentageService {

    private final CircularFifoQueue<Boolean> isBusyPerSecond = new CircularFifoQueue<>(120);

    public UserPercentageService(MessagePollerService messagePollerService) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                isBusyPerSecond.add(messagePollerService.isBusy());
            }
        }, 0, 500);
    }

    /**
     * @return the percentage of the last minute this worker was busy.
     */
    public double getBusyPercentage() {
        var current = isBusyPerSecond.stream().collect(Collectors.toUnmodifiableList());
        var busyCount = current.stream().filter((el) -> el).count();
        var totalCount = current.size();
        return (double) busyCount / (double) totalCount;
    }

}
