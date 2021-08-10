package eu.openanalytics.phaedra.phaedra2scriptengine;

import eu.openanalytics.phaedra.phaedra2scriptengine.config.data.Config;
import eu.openanalytics.phaedra.phaedra2scriptengine.model.runtime.ResponseStatusCode;
import eu.openanalytics.phaedra.phaedra2scriptengine.model.runtime.ScriptExecution;
import eu.openanalytics.phaedra.phaedra2scriptengine.model.runtime.ScriptExecutionInput;
import eu.openanalytics.phaedra.phaedra2scriptengine.service.executor.RExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

public class RExecutorUnitTest {

    @Test
    public void basicTest() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var myExecutor = new RExecutor(config);

        var scriptExecution = new ScriptExecution(new ScriptExecutionInput("myId", "output <- input$a + input$b", "{\"a\": 1, \"b\":2}", "myTopic"));
        Assertions.assertNull(scriptExecution.getWorkspace());

        var output = myExecutor.execute(scriptExecution);

        Assertions.assertEquals("{\"output\":3}\n", output.getOutput());
        Assertions.assertEquals("Ok", output.getStatusMessage());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output.getStatusCode());
        Assertions.assertEquals(0, output.getExitCode());
    }

    @Test
    public void checkWorkspaceFiles() throws InterruptedException, IOException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(false);

        var myExecutor = new RExecutor(config);

        var scriptExecution = new ScriptExecution(new ScriptExecutionInput("myId", "output <- input$a + input$b", "{\"a\": 1, \"b\":2}", "myTopic"));
        Assertions.assertNull(scriptExecution.getWorkspace());

        var output = myExecutor.execute(scriptExecution);

        Assertions.assertEquals("{\"output\":3}\n", output.getOutput());
        Assertions.assertEquals("Ok", output.getStatusMessage());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output.getStatusCode());
        Assertions.assertEquals(0, output.getExitCode());

        Assertions.assertEquals("{\"a\": 1, \"b\":2}", Files.readString(scriptExecution.getWorkspace().resolve("input.json")));
        Assertions.assertEquals(
            "fh <- file(\"input.json\")\n" +
            "input <- rjson::fromJSON(file=\"input.json\", simplify=TRUE)\n" +
            "close(fh)\n" +
            "output <- input$a + input$b\n" +
            "fh <- file(\"output.json\")\n" +
            "writeLines(rjson::toJSON(list(output = output)), fh)\n" +
            "close(fh)\n",
            Files.readString(scriptExecution.getWorkspace().resolve("script.R")));
        Assertions.assertEquals("{\"output\":3}\n", Files.readString(scriptExecution.getWorkspace().resolve("output.json")));

    }



}
