package eu.openanalytics.phaedra.scriptengine.watchdog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.scriptengine.dto.HeartbeatDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.watchdog.repository.ScriptExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static eu.openanalytics.phaedra.scriptengine.watchdog.WatchdogApplication.HEARTBEAT_QUEUE_NAME;
import static eu.openanalytics.phaedra.scriptengine.watchdog.WatchdogApplication.INPUT_QUEUE_NAME;
import static eu.openanalytics.phaedra.scriptengine.watchdog.WatchdogApplication.OUTPUT_QUEUE_NAME;

@Service
public class MessageListenerService implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScriptExecutionRepository scriptExecutionRepository;

    public MessageListenerService(ScriptExecutionRepository scriptExecutionRepository) {
        this.scriptExecutionRepository = scriptExecutionRepository;
    }

    @Override
    public void onMessage(Message message) {
        try {
            var routingKey = message.getMessageProperties().getReceivedRoutingKey();
            switch (message.getMessageProperties().getConsumerQueue()) {
                case INPUT_QUEUE_NAME ->   {
                    var input = objectMapper.readValue(message.getBody(), ScriptExecutionInputDTO.class);
                    onInput(input, routingKey);
                }
                case OUTPUT_QUEUE_NAME -> {
                    var output = objectMapper.readValue(message.getBody(), ScriptExecutionOutputDTO.class);
                    onOutput(output);
                }
                case HEARTBEAT_QUEUE_NAME -> {
                    var heartbeat = objectMapper.readValue(message.getBody(), HeartbeatDTO.class);
                    onHeartbeat(heartbeat);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onHeartbeat(HeartbeatDTO heartbeat) {
        logger.debug("Heartbeat: {}", heartbeat.getScriptExecutionId());
        scriptExecutionRepository.updateScriptExecution(heartbeat);
    }

    public void onInput(ScriptExecutionInputDTO input, String routingKey) {
        logger.debug("Input:      {}", input.getId());
        scriptExecutionRepository.createWatch(input, routingKey);
    }

    public void onOutput(ScriptExecutionOutputDTO output) {
        logger.debug("Output      {}", output.getInputId());
        scriptExecutionRepository.stopScriptExecution(output);
    }

}
