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

import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WatchdogApplication.class})
class WatchdogApplicationTests {

    @Autowired
    ScriptExecutionRepository watchService;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", Containers.postgreSQLContainer::getJdbcUrl);
        registry.add("DB_USER", Containers.postgreSQLContainer::getUsername);
        registry.add("DB_PASSWORD", Containers.postgreSQLContainer::getPassword);
    }

    @Test
    void contextLoads() throws InterruptedException, BrokenBarrierException {
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
            .workerName("my_worker")
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
                watchService.createWatch(input1, "scriptengine.input.fast-lane.JavaStat.v1");
                watchService.updateScriptExecution(input2);
            }
        });

        var thread2 = new Thread(() -> {
            try {
                gate.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
            watchService.updateScriptExecution(input2);
        });

        var thread3 = new Thread(() -> {
            try {
                gate.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
            watchService.stopScriptExecution(input3);
            watchService.updateScriptExecution(input2);
        });

        thread1.start();
        thread2.start();
        thread3.start();

        gate.await();

        thread1.join();
        thread2.join();

        var res = watchService.findById(id);
        Assertions.assertEquals(id, res.getId());
        Assertions.assertEquals("scriptengine.input.fast-lane.JavaStat.v1", res.getRoutingKey());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, res.getResponseStatusCode());
        Assertions.assertNotNull(res.getLastHeartbeat());
        Assertions.assertNotNull(res.getQueueTimestamp());
    }

}
