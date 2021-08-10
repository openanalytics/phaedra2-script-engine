package eu.openanalytics.phaedra.phaedra2scriptengine;

import eu.openanalytics.phaedra.phaedra2scriptengine.config.data.Config;
import eu.openanalytics.phaedra.phaedra2scriptengine.model.runtime.ResponseStatusCode;
import eu.openanalytics.phaedra.phaedra2scriptengine.model.runtime.ScriptExecutionOutput;
import eu.openanalytics.phaedra.phaedra2scriptengine.service.MessageProcessorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.data.util.Pair;

import java.nio.charset.StandardCharsets;

//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = ScriptEngineWorkerApplication.class)
// Unit Test
public class MessageProcessorServiceUnitTest {

    @Test
    public void basicTest() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var processor = new MessageProcessorService(
            scriptExecution -> new ScriptExecutionOutput(scriptExecution.getScriptExecutionInput(), "myOutput", ResponseStatusCode.SUCCESS, "Ok", 0),
            config);

        Pair<String, Message> response = processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"response_topic_suffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertEquals("scriptengine.output.myTopic", response.getFirst());
        Assertions.assertEquals("{\"status_message\":\"Ok\",\"output\":\"myOutput\",\"status_code\":\"SUCCESS\",\"exit_code\":0,\"input_id\":\"myId\"}", new String(response.getSecond().getBody(), StandardCharsets.UTF_8));
    }

    @Test
    public void invalidInput() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var processor = new MessageProcessorService(
            scriptExecution -> new ScriptExecutionOutput(scriptExecution.getScriptExecutionInput(), "myOutput", ResponseStatusCode.SUCCESS, "Ok", 0),
            config);

        Pair<String, Message> response = processor.processMessage(new Message("{\"}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNull(response);
    }

    @Test
    public void invalidInputMissingField() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var processor = new MessageProcessorService(
            scriptExecution -> new ScriptExecutionOutput(scriptExecution.getScriptExecutionInput(), "myOutput", ResponseStatusCode.SUCCESS, "Ok", 0),
            config);

        // missing id
        Assertions.assertNull(processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"response_topic_suffix\": \"myTopic\"}".getBytes(StandardCharsets.UTF_8))));

        // missing script
        Assertions.assertNull(processor.processMessage(new Message("{\"input\": \"myInput\", \"response_topic_suffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8))));

        // missing input
        Assertions.assertNull(processor.processMessage(new Message("{\"script\": \"myScript\", \"response_topic_suffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8))));

        // missing response_topic_suffix
        Assertions.assertNull(processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void exceptionInExecutor() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var processor = new MessageProcessorService(
            scriptExecution -> {
                throw new RuntimeException("oops");
            },
            config);

        Pair<String, Message> response = processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"response_topic_suffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNull(response);
    }


    @Test
    public void executorReturnsNull() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var processor = new MessageProcessorService(
            scriptExecution -> null,
            config);

        Pair<String, Message> response = processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"response_topic_suffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNull(response);
    }

    @Test
    public void executorReturnsInvalidResponse() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var processor = new MessageProcessorService(
            scriptExecution -> new ScriptExecutionOutput(null, null, null, null, 0),
            config);

        Pair<String, Message> response = processor.processMessage(new Message("{\"script\": \"myScript\", \"input\": \"myInput\", \"response_topic_suffix\": \"myTopic\", \"id\": \"myId\"}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNull(response);
    }

}
