/**
 * Phaedra II
 *
 * Copyright (C) 2016-2022 Open Analytics
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
package eu.openanalytics.phaedra.scriptengine.watchdog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "phaedra2.script-engine-watchdog")
public class WatchDogConfig {

    private List<Target> targets = new ArrayList<>();

    /**
     * @return the prefix of the topic used in output messages on the output exchange.
     */
    public String getOutputRoutingKeyPrefix() {
        return "scriptengine.output.";
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }

    public static class Target {

        private String routingKey;
        private int heartbeatInterval;
        private int maxMissedHeartbeats;


        public String getRoutingKey() {
            return routingKey;
        }

        public int getHeartbeatInterval() {
            return heartbeatInterval;
        }

        public int getMaxMissedHeartbeats() {
            return maxMissedHeartbeats;
        }

        public void setMaxMissedHeartbeats(int maxMissedHeartbeats) {
            this.maxMissedHeartbeats = maxMissedHeartbeats;
        }

        public void setHeartbeatInterval(int heartbeatInterval) {
            this.heartbeatInterval = heartbeatInterval;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }
    }

}
