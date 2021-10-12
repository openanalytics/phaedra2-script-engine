package eu.openanalytics.phaedra.scriptengine.watchdog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.scriptengine.dto.HeartbeatDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.watchdog.repository.ScriptExecutionRepository;
import eu.openanalytics.phaedra.scriptengine.watchdog.service.MessageListenerService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;

import static eu.openanalytics.phaedra.scriptengine.watchdog.WatchdogApplication.HEARTBEAT_QUEUE_NAME;
import static eu.openanalytics.phaedra.scriptengine.watchdog.WatchdogApplication.INPUT_QUEUE_NAME;
import static eu.openanalytics.phaedra.scriptengine.watchdog.WatchdogApplication.OUTPUT_QUEUE_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class MessageListenerTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void onInputMessage() throws JsonProcessingException {
        var repository = mock(ScriptExecutionRepository.class);
        var listener = new MessageListenerService(repository);

        var msgProps = new MessageProperties();
        msgProps.setConsumerQueue(INPUT_QUEUE_NAME);
        msgProps.setReceivedRoutingKey("scriptengine.input.fast-lane.JavaStat.v1");

        var script1 = new ScriptExecutionInputDTO("myId1", "theScript", "theInput", "CalculationService", 10);
        var input1 = new Message(objectMapper.writeValueAsBytes(script1), msgProps);

        listener.onMessage(input1);

        verify(repository).createScriptExecution(script1, "scriptengine.input.fast-lane.JavaStat.v1");
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void onOutputMessage() throws JsonProcessingException {
        var repository = mock(ScriptExecutionRepository.class);
        var listener = new MessageListenerService(repository);

        var msgProps = new MessageProperties();
        msgProps.setConsumerQueue(OUTPUT_QUEUE_NAME);

        var output = new ScriptExecutionOutputDTO("myId1", "someOutput", ResponseStatusCode.SUCCESS, "Ok", 0);
        var input1 = new Message(objectMapper.writeValueAsBytes(output), msgProps);

        listener.onMessage(input1);

        verify(repository).stopScriptExecution(output );
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void onHeartbeatMessage() throws JsonProcessingException {
        var repository = mock(ScriptExecutionRepository.class);
        var listener = new MessageListenerService(repository);

        var msgProps = new MessageProperties();
        msgProps.setConsumerQueue(HEARTBEAT_QUEUE_NAME);

        var heartbeat = new HeartbeatDTO("myId1");
        var input1 = new Message(objectMapper.writeValueAsBytes(heartbeat), msgProps);

        listener.onMessage(input1);

        verify(repository).updateScriptExecution(heartbeat);
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void invalidMessage() {
        var repository = mock(ScriptExecutionRepository.class);
        var listener = new MessageListenerService(repository);

        var msgProps = new MessageProperties();
        msgProps.setConsumerQueue(HEARTBEAT_QUEUE_NAME);

        var input1 = new Message("{\"scriptExecutionId:".getBytes(StandardCharsets.UTF_8), msgProps);

        listener.onMessage(input1);

        verifyNoMoreInteractions(repository);
    }
}
