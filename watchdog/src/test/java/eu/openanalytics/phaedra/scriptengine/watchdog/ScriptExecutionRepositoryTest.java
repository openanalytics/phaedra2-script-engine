package eu.openanalytics.phaedra.scriptengine.watchdog;

import eu.openanalytics.phaedra.scriptengine.dto.HeartbeatDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.watchdog.repository.ScriptExecutionRepository;
import eu.openanalytics.phaedra.scriptengine.watchdog.support.Containers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WatchdogApplication.class})
class ScriptExecutionRepositoryTest {

    @Autowired
    ScriptExecutionRepository repository;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_HOST_PORT", () -> String.format("%s:%s", Containers.postgreSQLContainer.getHost(), Containers.postgreSQLContainer.getMappedPort(5432)));
        registry.add("DB_NAME", Containers.postgreSQLContainer::getDatabaseName);
        registry.add("DB_USER", Containers.postgreSQLContainer::getUsername);
        registry.add("DB_PASSWORD", Containers.postgreSQLContainer::getPassword);
    }

    @Test
    void complexTest() throws InterruptedException, BrokenBarrierException {
        var id = UUID.randomUUID().toString();

        var input1 = ScriptExecutionInputDTO.builder()
            .id(id)
            .input("my_input")
            .queueTimestamp(System.currentTimeMillis())
            .script("my_script")
            .responseTopicSuffix("my_topic")
            .build();

        var input2 = HeartbeatDTO.builder()
            .scriptExecutionId(id)
            .build();

        var input3 = ScriptExecutionOutputDTO.builder()
            .inputId(id)
            .statusCode(ResponseStatusCode.SUCCESS)
            .output("some_output")
            .statusMessage("Ok")
            .exitCode(0)
            .build();

        final CyclicBarrier gate = new CyclicBarrier(4);
        var thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    gate.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                repository.createScriptExecution(input1, "scriptengine.input.fast-lane.JavaStat.v1");
                repository.updateScriptExecution(input2);
            }
        });

        var thread2 = new Thread(() -> {
            try {
                gate.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
            repository.updateScriptExecution(input2);
        });

        var thread3 = new Thread(() -> {
            try {
                gate.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
            repository.stopScriptExecution(input3);
            repository.updateScriptExecution(input2);
        });

        thread1.start();
        thread2.start();
        thread3.start();

        gate.await();

        thread1.join();
        thread2.join();

        var res = repository.findById(id);
        Assertions.assertEquals(id, res.getId());
        Assertions.assertEquals("scriptengine.input.fast-lane.JavaStat.v1", res.getInputRoutingKey());
        Assertions.assertEquals("scriptengine.output.my_topic", res.getOutputRoutingKey());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, res.getResponseStatusCode());
        Assertions.assertNotNull(res.getLastHeartbeat());
        Assertions.assertNotNull(res.getQueueTimestamp());
    }

    @Test
    public void findToInterrupt() {
        var id = UUID.randomUUID().toString();
        var input1 = ScriptExecutionInputDTO.builder()
            .id(id)
            .input("my_input")
            .queueTimestamp(System.currentTimeMillis())
            .script("my_script")
            .responseTopicSuffix("my_topic")
            .build();

        repository.createScriptExecution(input1, "scriptengine.input.fast-lane.JavaStat.v1");
        var input2 = HeartbeatDTO.builder()
            .scriptExecutionId(id)
            .build();
        repository.updateScriptExecution(input2);

        var res2 = repository.findToInterrupt("scriptengine.input.fast-lane.JavaStat.v1", LocalDateTime.now().plusSeconds(10));
        Assertions.assertEquals(1, res2.size());
        Assertions.assertEquals(id, res2.get(0).getId());

        var res3 = repository.findToInterrupt("scriptengine.input.fast-lane.JavaStat.v1", LocalDateTime.now().minusSeconds(10));
        Assertions.assertEquals(0, res3.size());

        var res4 = repository.findToInterrupt("some_other_topic", LocalDateTime.now());
        Assertions.assertEquals(0, res4.size());

        // stop scriptExecution
        var input5= ScriptExecutionOutputDTO.builder()
            .inputId(id)
            .statusCode(ResponseStatusCode.SUCCESS)
            .output("some_output")
            .statusMessage("Ok")
            .exitCode(0)
            .build();

        repository.stopScriptExecution(input5);

        var res6 = repository.findToInterrupt("scriptengine.input.fast-lane.JavaStat.v1", LocalDateTime.now().plusSeconds(10));
        Assertions.assertEquals(0, res6.size());

        var res7 = repository.findToInterrupt("scriptengine.input.fast-lane.JavaStat.v1", LocalDateTime.now().minusSeconds(10));
        Assertions.assertEquals(0, res7.size());

    }

}
