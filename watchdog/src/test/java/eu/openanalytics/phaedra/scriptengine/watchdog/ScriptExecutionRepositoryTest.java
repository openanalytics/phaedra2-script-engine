package eu.openanalytics.phaedra.scriptengine.watchdog;

import eu.openanalytics.phaedra.scriptengine.dto.HeartbeatDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.watchdog.repository.ScriptExecutionRepository;
import eu.openanalytics.phaedra.scriptengine.watchdog.support.Containers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    /**
     * Clean tables and sequences before every test (this is aster than restarting the container and Spring context).
     */
    @BeforeEach
    public void initEach() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement stmt = con.prepareStatement("TRUNCATE script_execution RESTART IDENTITY CASCADE;")) {
                stmt.executeUpdate();
            }
        }
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_HOST_PORT", () -> String.format("%s:%s", Containers.postgreSQLContainer.getHost(), Containers.postgreSQLContainer.getMappedPort(5432)));
        registry.add("DB_NAME", Containers.postgreSQLContainer::getDatabaseName);
        registry.add("DB_USER", Containers.postgreSQLContainer::getUsername);
        registry.add("DB_PASSWORD", Containers.postgreSQLContainer::getPassword);
        registry.add("spring.rabbitmq.host", Containers.rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", Containers.rabbitMQContainer::getAmqpPort);
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
        thread3.join();

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
        var input5 = ScriptExecutionOutputDTO.builder()
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

    @Test
    public void deleteExpiredEntriesTest() {
        var query = "INSERT INTO script_execution (id, input_routing_key, queue_timestamp, last_heartbeat, response_status_code, output_routing_key) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(query, "28686c8e-1032-4625-9765-aefd512b27f9", "scriptengine.input.fast-lane.R.v1",
            LocalDateTime.now().minusMinutes(78), LocalDateTime.now().minusMinutes(80), "SUCCESS", "scriptengine.output.CalculationService");

        jdbcTemplate.update(query, "75579c96-d4f2-4189-9c54-14a8208843df", "scriptengine.input.fast-lane.R.v1",
            LocalDateTime.now().minusMinutes(3), LocalDateTime.now().minusMinutes(2), "SUCCESS", "scriptengine.output.CalculationService");

        jdbcTemplate.update(query, "b9974eeb-0e63-4c74-a5c7-369ac6cadbc7", "scriptengine.input.fast-lane.R.v1",
            LocalDateTime.now().minusSeconds(10), LocalDateTime.now().minusSeconds(2), null, "scriptengine.output.CalculationService");


        var numRemoved = repository.deleteExpiredEntries();
        Assertions.assertEquals(1, numRemoved);
        var entries = repository.findAll();
        Assertions.assertEquals(2, entries.size());
        Assertions.assertEquals("75579c96-d4f2-4189-9c54-14a8208843df", entries.get(0).getId());
        Assertions.assertEquals("b9974eeb-0e63-4c74-a5c7-369ac6cadbc7", entries.get(1).getId());
    }

}
