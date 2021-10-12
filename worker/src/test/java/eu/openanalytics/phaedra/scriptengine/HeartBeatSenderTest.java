package eu.openanalytics.phaedra.scriptengine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.scriptengine.config.EnvConfig;
import eu.openanalytics.phaedra.scriptengine.dto.HeartbeatDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.service.HeartbeatSenderService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static eu.openanalytics.phaedra.scriptengine.ScriptEngineWorkerApplication.HEARTBEAT_EXCHANGE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class HeartBeatSenderTest {

    RabbitTemplate rabbiTemplate = mock(RabbitTemplate.class);
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void basicTest() throws JsonProcessingException, InterruptedException {
        var envConfig = new EnvConfig();
        envConfig.setHeartbeatInterval(2);
        var heartBeatSender = new HeartbeatSenderService(envConfig, rabbiTemplate, objectMapper);

        var input = new ScriptExecutionInputDTO("myId", "", "", "", 10);

        // start heartbeats
        heartBeatSender.sendAndStartHeartbeats(input);

        // sleep 10 seconds
        Thread.sleep(10000);

        // stop heartbeats
        heartBeatSender.stopHeartbeats(input);

        // expect 5+1 heartbeats
        var msg = new Message(objectMapper.writeValueAsBytes(HeartbeatDTO.builder().scriptExecutionId("myId").build()));
        verify(rabbiTemplate, times(6)).send(HEARTBEAT_EXCHANGE, "heartbeat", msg);
    }

    @Test
    public void complexTest() throws JsonProcessingException, InterruptedException {
        var envConfig = new EnvConfig();
        envConfig.setHeartbeatInterval(2);
        var heartBeatSender = new HeartbeatSenderService(envConfig, rabbiTemplate, objectMapper);

        var input1 = new ScriptExecutionInputDTO("id1", "", "", "", 10);
        var input2 = new ScriptExecutionInputDTO("id2", "", "", "", 10);
        var input3 = new ScriptExecutionInputDTO("id3", "", "", "", 10);

        // start heartbeat for input1
        heartBeatSender.sendAndStartHeartbeats(input1);

        // sleep 2 seconds
        Thread.sleep(2000);

        // start heartbeat for input2
        heartBeatSender.sendAndStartHeartbeats(input2);

        // sleep 2 seconds
        Thread.sleep(2000);

        // stop heartbeat for input1
        heartBeatSender.stopHeartbeats(input1);

        // sleep 4 seconds
        Thread.sleep(4000);

        // send heartbeat for input3
        heartBeatSender.sendAndStartHeartbeats(input3);

        // sleep 4 seconds
        Thread.sleep(4000);

        // stop heartbeat for input2
        heartBeatSender.stopHeartbeats(input2);

        // sleep 4 seconds
        Thread.sleep(4000);

        // stop heartbeat for input3
        heartBeatSender.stopHeartbeats(input3);

        // expect 2+1 heartbeats for id1
        var msg1 = new Message(objectMapper.writeValueAsBytes(HeartbeatDTO.builder().scriptExecutionId("id1").build()));
        verify(rabbiTemplate, times(3)).send(HEARTBEAT_EXCHANGE, "heartbeat", msg1);

        // expect 5+1 heartbeats for id1
        var msg2 = new Message(objectMapper.writeValueAsBytes(HeartbeatDTO.builder().scriptExecutionId("id2").build()));
        verify(rabbiTemplate, times(6)).send(HEARTBEAT_EXCHANGE, "heartbeat", msg2);

        // expect 4+1 heartbeats for id1
        var msg3 = new Message(objectMapper.writeValueAsBytes(HeartbeatDTO.builder().scriptExecutionId("id3").build()));
        verify(rabbiTemplate, times(5)).send(HEARTBEAT_EXCHANGE, "heartbeat", msg3);
    }

}
