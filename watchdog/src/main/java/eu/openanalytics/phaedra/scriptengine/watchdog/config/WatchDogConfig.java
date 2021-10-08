package eu.openanalytics.phaedra.scriptengine.watchdog.config;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WatchDogConfig {

    public List<WorkerQueue> getInputQueues() {
        return List.of(
            new WorkerQueue("scriptengine.input.fast-lane.JavaStat.v1", 5000, 5000),
            new WorkerQueue("scriptengine.input.fast-lane.R.v1", 5000, 5000));
    }

    public String getOutputRoutingKey() {
        return "scriptengine.output.CalculationService";
    }


    public static class WorkerQueue {

        private String routingKey;
        private int initialDelay;
        private int heartBeatInterval;

        private WorkerQueue(String routingKey, int initialDelay, int heartBeatInterval) {
            this.routingKey = routingKey;
            this.initialDelay = initialDelay;
            this.heartBeatInterval = heartBeatInterval;
        }

        public int getHeartBeatInterval() {
            return heartBeatInterval;
        }

        public int getInitialDelay() {
            return initialDelay;
        }

        public String getRoutingKey() {
            return routingKey;
        }
    }

}
