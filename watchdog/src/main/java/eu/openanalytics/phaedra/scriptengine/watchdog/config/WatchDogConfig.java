package eu.openanalytics.phaedra.scriptengine.watchdog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "phaedra2.script-engine-watchdog")
public class WatchDogConfig {

    private List<Target> targets;

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
