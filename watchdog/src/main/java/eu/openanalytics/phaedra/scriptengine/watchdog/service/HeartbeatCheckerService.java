package eu.openanalytics.phaedra.scriptengine.watchdog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.watchdog.config.WatchDogConfig;
import eu.openanalytics.phaedra.scriptengine.watchdog.model.ScriptExecution;
import eu.openanalytics.phaedra.scriptengine.watchdog.repository.ScriptExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

import static eu.openanalytics.phaedra.scriptengine.watchdog.WatchdogApplication.OUTPUT_EXCHANGE;

@Service
public class HeartbeatCheckerService {

    private final Timer timer = new Timer();
    private final ScriptExecutionRepository scriptExecutionRepository;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public HeartbeatCheckerService(WatchDogConfig config, ScriptExecutionRepository scriptExecutionRepository, ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) {
        this.scriptExecutionRepository = scriptExecutionRepository;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
        for (var target : config.getTargets()) {
            var scheduleRate = Math.max(1, (target.getHeartbeatInterval() / 2));
            timer.schedule(new TargetSpecificChecker(target), scheduleRate * 1000L, scheduleRate * 1000L);
        }
    }

    public class TargetSpecificChecker extends TimerTask {

        private final WatchDogConfig.Target target;
        private final Logger logger = LoggerFactory.getLogger(getClass());

        public TargetSpecificChecker(WatchDogConfig.Target target) {
            this.target = target;
        }

        @Override
        public void run() {
            try {
                var routingKey = target.getRoutingKey();
                var now = LocalDateTime.now();
                var notBefore = now.minusSeconds((long) target.getHeartbeatInterval() * target.getMaxMissedHeartbeats());
                for (var scriptExecution : scriptExecutionRepository.findToInterrupt(routingKey, notBefore)) {
                    interruptScriptExecution(scriptExecution, now);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void interruptScriptExecution(ScriptExecution scriptExecution, LocalDateTime now) {
            var secondsSinceLastHeartbeat = Duration.between(scriptExecution.getLastHeartbeat(), now).toMillis();
            var statusMessage = String.format("Rescheduled by WatchDog because heartbeat was %sms ago (expects to receive heartbeat every %s seconds and allow to miss less than %s heartbeats)", secondsSinceLastHeartbeat, target.getHeartbeatInterval(), target.getMaxMissedHeartbeats());
            logger.warn("ScriptExecution with id {} {} ", scriptExecution.getId(), statusMessage);
            var output = ScriptExecutionOutputDTO.builder()
                .inputId(scriptExecution.getId())
                .statusCode(ResponseStatusCode.RESCHEDULED_BY_WATCHDOG)
                .statusMessage(statusMessage)
                .exitCode(0)
                .output("")
                .build();
            scriptExecutionRepository.stopScriptExecution(output);

            try {
                rabbitTemplate.send(OUTPUT_EXCHANGE, scriptExecution.getOutputRoutingKey(), new Message(objectMapper.writeValueAsBytes(output)));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

    }

}
