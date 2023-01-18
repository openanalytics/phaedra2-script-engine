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
package eu.openanalytics.phaedra.scriptengine.watchdog.service;

import eu.openanalytics.phaedra.scriptengine.watchdog.repository.ScriptExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A service that removes stopped {@see ScriptExecution} after they have been finished for at least one hour.
 * The idea of delaying this cleanup is to prevent an entry to be re-created just after it has been removed.
 * Because the WatchDog listens to multiple queues, the messages in these queues are not deliver in any specific order.
 * Therefore, the following may happen:
 *
 * - input queue receives an input message for id 1
 * - output queue receives an output message for id 1
 * - heartbeat queue receives a heartbeat message for id 1
 *
 * If the WatchDog had removed the entry from the database when the output message is received, a new entry would have been created
 * when the heartbeat was received. In addition, this message would never be removed from the database again.
 *
 * By waiting one hour to cleanup entries, we are sure that no messages are in transit.
 */
@Service
public class ScriptExecutionCleaner {

    private Timer timer = new Timer();

    private Logger logger = LoggerFactory.getLogger(getClass());

    public ScriptExecutionCleaner(ScriptExecutionRepository scriptExecutionRepository) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                var num = scriptExecutionRepository.deleteExpiredEntries();
                logger.info("Removed {} entries from script_execution table", num);
            }
        }, 1000, 60 * 60 * 1000);
    }

}
