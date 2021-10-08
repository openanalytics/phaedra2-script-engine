package eu.openanalytics.phaedra.scriptengine.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.scriptengine.dto.HeartbeatDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.event.ScriptProcessedEvent;
import eu.openanalytics.phaedra.scriptengine.event.ScriptReceivedEvent;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static eu.openanalytics.phaedra.scriptengine.ScriptEngineWorkerApplication.HEARTBEAT_EXCHANGE;

@Service
public class HeartbeatSenderService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public HeartbeatSenderService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Async
    @EventListener
    public void onScriptProcessedEvent(ScriptProcessedEvent event) {
    }

    @Async
    @EventListener
    public void onScriptReceivedEvent(ScriptReceivedEvent event) {
    }

    public void sendExecutionStarted(ScriptExecutionInputDTO input) throws JsonProcessingException {
        var msg = new Message(objectMapper.writeValueAsBytes(HeartbeatDTO.builder().scriptExecutionId(input.getId()).workerName("main").build()));
        rabbitTemplate.send(HEARTBEAT_EXCHANGE, "heartbeat", msg);
    }
}
