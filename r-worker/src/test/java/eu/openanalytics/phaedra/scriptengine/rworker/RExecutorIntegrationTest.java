/**
 * Phaedra II
 *
 * Copyright (C) 2016-2023 Open Analytics
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
package eu.openanalytics.phaedra.scriptengine.rworker;

import eu.openanalytics.phaedra.scriptengine.config.ExternalProcessConfig;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.model.runtime.ScriptExecution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

@Disabled
public class RExecutorIntegrationTest {

    @Test
    public void basicTest() throws InterruptedException {
        var config = new ExternalProcessConfig();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var myExecutor = new RExecutor(config);

        var scriptExecution = new ScriptExecution(ScriptExecutionInputDTO.builder()
        		.id("myId").script("output <- input$a + input$b").input("{\"a\": 1, \"b\":2}").build());
        Assertions.assertNull(scriptExecution.getWorkspace());

        var output = myExecutor.execute(scriptExecution);

        Assertions.assertEquals("{\"output\":3}\n", output.getOutput());
        Assertions.assertEquals("Ok", output.getStatusMessage());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output.getStatusCode());
    }

    @Test
    public void checkWorkspaceFiles() throws InterruptedException, IOException {
        var config = new ExternalProcessConfig();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(false);

        var myExecutor = new RExecutor(config);

        var scriptExecution = new ScriptExecution(ScriptExecutionInputDTO.builder()
        		.id("myId").script("output <- input$a + input$b").input("{\"a\": 1, \"b\":2}").build());
        Assertions.assertNull(scriptExecution.getWorkspace());

        var output = myExecutor.execute(scriptExecution);

        Assertions.assertEquals("{\"output\":3}\n", output.getOutput());
        Assertions.assertEquals("Ok", output.getStatusMessage());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output.getStatusCode());

        Assertions.assertEquals("{\"a\": 1, \"b\":2}", Files.readString(scriptExecution.getWorkspace().resolve("input.json")));
        Assertions.assertEquals("library(jsonlite)\n"
            + "input <- fromJSON(\"input.json\")\n"
            + "output <- input$a + input$b\n"
            + "json_obj <- toJSON(list(output = output), pretty = TRUE)\n"
            + "writeLines(json_obj, \"output.json\")",
            Files.readString(scriptExecution.getWorkspace().resolve("script.R")));
//        Assertions.assertEquals(
//            "fh <- file(\"input.json\")\n" +
//                "input <- rjson::fromJSON(file=\"input.json\", simplify=TRUE)\n" +
//                "close(fh)\n" +
//                "output <- input$a + input$b\n" +
//                "fh <- file(\"output.json\")\n" +
//                "writeLines(rjson::toJSON(list(output = output)), fh)\n" +
//                "close(fh)\n",
//            Files.readString(scriptExecution.getWorkspace().resolve("script.R")));
        Assertions.assertEquals("{\"output\":3}\n", Files.readString(scriptExecution.getWorkspace().resolve("output.json")));

    }


}
