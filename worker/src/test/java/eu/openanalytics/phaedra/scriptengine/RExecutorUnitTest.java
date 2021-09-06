/**
 * ContainerProxy
 *
 * Copyright (C) 2016-2021 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.scriptengine;

import eu.openanalytics.phaedra.scriptengine.config.data.Config;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.model.runtime.ScriptExecution;
import eu.openanalytics.phaedra.scriptengine.service.executor.RExecutor;
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

        var scriptExecution = new ScriptExecution(new ScriptExecutionInputDTO("myId", "output <- input$a + input$b", "{\"a\": 1, \"b\":2}", "myTopic", System.currentTimeMillis()));
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

        var scriptExecution = new ScriptExecution(new ScriptExecutionInputDTO("myId", "output <- input$a + input$b", "{\"a\": 1, \"b\":2}", "myTopic", System.currentTimeMillis()));
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
