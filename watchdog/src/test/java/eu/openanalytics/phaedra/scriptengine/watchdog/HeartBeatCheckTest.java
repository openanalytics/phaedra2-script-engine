package eu.openanalytics.phaedra.scriptengine.watchdog;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.watchdog.config.WatchDogConfig;
import eu.openanalytics.phaedra.scriptengine.watchdog.model.ScriptExecution;
import eu.openanalytics.phaedra.scriptengine.watchdog.repository.ScriptExecutionRepository;
import eu.openanalytics.phaedra.scriptengine.watchdog.service.HeartbeatCheckerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static eu.openanalytics.phaedra.scriptengine.watchdog.WatchdogApplication.OUTPUT_EXCHANGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class HeartBeatCheckTest {

    private RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private ScriptExecutionRepository repository = mock(ScriptExecutionRepository.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void simpleNoScriptsToInterrupt() throws InterruptedException {
        var target1 = new WatchDogConfig.Target();
        target1.setHeartbeatInterval(2);
        target1.setMaxMissedHeartbeats(2);
        target1.setRoutingKey("myRoutingKey1");

        var config = new WatchDogConfig();
        config.setTargets(List.of(target1));

        new HeartbeatCheckerService(config, repository, objectMapper, rabbitTemplate);

        Thread.sleep(5000);

        // repository should have been checked 5 times now
        verify(repository, times(5)).findToInterrupt(eq("myRoutingKey1"), any());

        verifyNoMoreInteractions(repository, rabbitTemplate);
    }


    @Test
    public void simpleScriptsToInterrupt() throws InterruptedException {
        var outputCaptor = ArgumentCaptor.forClass(ScriptExecutionOutputDTO.class);
        var routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        var messageCaptor = ArgumentCaptor.forClass(Message.class);

        var target1 = new WatchDogConfig.Target();
        target1.setHeartbeatInterval(2);
        target1.setMaxMissedHeartbeats(2);
        target1.setRoutingKey("myRoutingKey1");

        var config = new WatchDogConfig();
        config.setTargets(List.of(target1));

        new HeartbeatCheckerService(config, repository, objectMapper, rabbitTemplate);

        doReturn(List.of(
            new ScriptExecution("myId1", "myRoutingKey1", "scriptengine.output.CalculationService", LocalDateTime.now().minusSeconds(10), LocalDateTime.now().minusSeconds(2), null),
            new ScriptExecution("myId2", "myRoutingKey2", "scriptengine.output.CalculationService", LocalDateTime.now().minusSeconds(8), LocalDateTime.now().minusSeconds(4), null),
            new ScriptExecution("myId3", "myRoutingKey3", "scriptengine.output.CalculationService", LocalDateTime.now().minusSeconds(12), LocalDateTime.now().minusSeconds(3), null)
        ), List.of())
            .when(repository).findToInterrupt(eq("myRoutingKey1"), any());

        Thread.sleep(2000);

        verify(repository, times(3)).stopScriptExecution(outputCaptor.capture());
        verify(rabbitTemplate, times(3)).send(eq(OUTPUT_EXCHANGE), routingKeyCaptor.capture(), messageCaptor.capture());

        Assertions.assertEquals(3, outputCaptor.getAllValues().size());

        Assertions.assertEquals("myId1", outputCaptor.getAllValues().get(0).getInputId());
        Assertions.assertEquals(ResponseStatusCode.RESCHEDULED_BY_WATCHDOG, outputCaptor.getAllValues().get(0).getStatusCode());
        assertThat(outputCaptor.getAllValues().get(0).getStatusMessage(), matchesPattern("^Rescheduled by WatchDog because heartbeat was [0-9]*ms ago \\(expects to receive heartbeat every 2 seconds and allow to miss less than 2 heartbeats\\)$"));

        Assertions.assertEquals("myId2", outputCaptor.getAllValues().get(1).getInputId());
        Assertions.assertEquals(ResponseStatusCode.RESCHEDULED_BY_WATCHDOG, outputCaptor.getAllValues().get(1).getStatusCode());
        assertThat(outputCaptor.getAllValues().get(1).getStatusMessage(), matchesPattern("^Rescheduled by WatchDog because heartbeat was [0-9]*ms ago \\(expects to receive heartbeat every 2 seconds and allow to miss less than 2 heartbeats\\)$"));

        Assertions.assertEquals("myId3", outputCaptor.getAllValues().get(2).getInputId());
        Assertions.assertEquals(ResponseStatusCode.RESCHEDULED_BY_WATCHDOG, outputCaptor.getAllValues().get(2).getStatusCode());
        assertThat(outputCaptor.getAllValues().get(2).getStatusMessage(), matchesPattern("^Rescheduled by WatchDog because heartbeat was [0-9]*ms ago \\(expects to receive heartbeat every 2 seconds and allow to miss less than 2 heartbeats\\)$"));

        Assertions.assertEquals(3, routingKeyCaptor.getAllValues().size());
        Assertions.assertEquals("scriptengine.output.CalculationService", routingKeyCaptor.getAllValues().get(0));
        Assertions.assertEquals("scriptengine.output.CalculationService", routingKeyCaptor.getAllValues().get(1));
        Assertions.assertEquals("scriptengine.output.CalculationService", routingKeyCaptor.getAllValues().get(2));
        Assertions.assertEquals(3, messageCaptor.getAllValues().size());
        verifyNoMoreInteractions(rabbitTemplate);
    }

}
