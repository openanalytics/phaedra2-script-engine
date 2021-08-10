package eu.openanalytics.phaedra.phaedra2scriptengine;

import eu.openanalytics.phaedra.phaedra2scriptengine.config.data.Config;
import eu.openanalytics.phaedra.phaedra2scriptengine.model.runtime.ResponseStatusCode;
import eu.openanalytics.phaedra.phaedra2scriptengine.model.runtime.ScriptExecution;
import eu.openanalytics.phaedra.phaedra2scriptengine.model.runtime.ScriptExecutionInput;
import eu.openanalytics.phaedra.phaedra2scriptengine.service.executor.AbstractExecutor;
import eu.openanalytics.phaedra.phaedra2scriptengine.service.executor.WorkerException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class AbstractExecutorUnitTest {

    @Test
    public void basicTest() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var myExecutor = new AbstractExecutor(config) {
            @Override
            protected String getFullScript(ScriptExecution scriptExecution) {
                return "my-header\n" + scriptExecution.getScriptExecutionInput().getScript() + "\nmy-footer\n";
            }

            @Override
            protected String getScriptName(ScriptExecution scriptExecution) {
                return "myScript.txt";
            }

            @Override
            protected int executeScript(ScriptExecution scriptExecution) {
                try {
                    Files.write(scriptExecution.getWorkspace().resolve("output.json"), "myOutput".getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return 42;
            }
        };

        var scriptExecution = new ScriptExecution(new ScriptExecutionInput("myId", "myScript\nsecond-line", "myInput", "myTopic"));
        Assertions.assertNull(scriptExecution.getWorkspace());

        var output = myExecutor.execute(scriptExecution);

        Assertions.assertEquals("myOutput", output.getOutput());
        Assertions.assertEquals("Ok", output.getStatusMessage());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output.getStatusCode());
        Assertions.assertEquals(42, output.getExitCode());
    }

    @Test
    public void scriptProducesNoOutput() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var myExecutor = new AbstractExecutor(config) {
            @Override
            protected String getFullScript(ScriptExecution scriptExecution) {
                return "my-header\n" + scriptExecution.getScriptExecutionInput().getScript() + "\nmy-footer\n";
            }

            @Override
            protected String getScriptName(ScriptExecution scriptExecution) {
                return "myScript.txt";
            }

            @Override
            protected int executeScript(ScriptExecution scriptExecution) {
                return 42;
            }
        };

        var scriptExecution = new ScriptExecution(new ScriptExecutionInput("myId", "myScript\nsecond-line", "myInput", "myTopic"));
        Assertions.assertNull(scriptExecution.getWorkspace());

        var output = myExecutor.execute(scriptExecution);

        Assertions.assertEquals("", output.getOutput());
        Assertions.assertEquals("Script did not create output file!", output.getStatusMessage());
        Assertions.assertEquals(ResponseStatusCode.SCRIPT_ERROR, output.getStatusCode());
        Assertions.assertEquals(42, output.getExitCode());
    }

    @Test
    public void scriptProducesWorkerException() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var myExecutor = new AbstractExecutor(config) {
            @Override
            protected String getFullScript(ScriptExecution scriptExecution) {
                return "my-header\n" + scriptExecution.getScriptExecutionInput().getScript() + "\nmy-footer\n";
            }

            @Override
            protected String getScriptName(ScriptExecution scriptExecution) {
                return "myScript.txt";
            }

            @Override
            protected int executeScript(ScriptExecution scriptExecution) throws WorkerException {
                throw new WorkerException("oops", new Throwable());
            }
        };

        var scriptExecution = new ScriptExecution(new ScriptExecutionInput("myId", "myScript\nsecond-line", "myInput", "myTopic"));
        Assertions.assertNull(scriptExecution.getWorkspace());

        var output = myExecutor.execute(scriptExecution);

        Assertions.assertEquals("", output.getOutput());
        Assertions.assertEquals("An error occurred in the worker while processing the script.", output.getStatusMessage());
        Assertions.assertEquals(ResponseStatusCode.WORKER_INTERNAL_ERROR, output.getStatusCode());
        Assertions.assertEquals(-1, output.getExitCode()); // TODO is this a good status code?
    }

    @Test
    public void testWorkspaceFiles() throws InterruptedException, IOException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(false);

        var myExecutor = new AbstractExecutor(config) {
            @Override
            protected String getFullScript(ScriptExecution scriptExecution) {
                return "my-header\n" + scriptExecution.getScriptExecutionInput().getScript() + "\nmy-footer\n";
            }

            @Override
            protected String getScriptName(ScriptExecution scriptExecution) {
                return "myScript.txt";
            }

            @Override
            protected int executeScript(ScriptExecution scriptExecution) throws WorkerException {
                try {
                    Files.write(scriptExecution.getWorkspace().resolve("output.json"), "myOutput".getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return 42;
            }
        };

        var scriptExecution = new ScriptExecution(new ScriptExecutionInput("myId", "myScript\nsecond-line", "myInput", "myTopic"));
        Assertions.assertNull(scriptExecution.getWorkspace());

        var output = myExecutor.execute(scriptExecution);

        Assertions.assertEquals("myOutput", output.getOutput());
        Assertions.assertEquals("Ok", output.getStatusMessage());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output.getStatusCode());
        Assertions.assertEquals(42, output.getExitCode());

        Assertions.assertTrue(scriptExecution.getWorkspace().resolve("input.json").toFile().exists());
        Assertions.assertTrue(scriptExecution.getWorkspace().resolve("myScript.txt").toFile().exists());
        Assertions.assertTrue(scriptExecution.getWorkspace().resolve("output.json").toFile().exists());

        FileSystemUtils.deleteRecursively(scriptExecution.getWorkspace());
    }

    @Test
    public void testCleanWorkspaceEvenIfWorkerException() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var myExecutor = new AbstractExecutor(config) {
            @Override
            protected String getFullScript(ScriptExecution scriptExecution) {
                return "my-header\n" + scriptExecution.getScriptExecutionInput().getScript() + "\nmy-footer\n";
            }

            @Override
            protected String getScriptName(ScriptExecution scriptExecution) {
                return "myScript.txt";
            }

            @Override
            protected int executeScript(ScriptExecution scriptExecution) throws WorkerException {
                throw new WorkerException("oops", new Throwable());
            }
        };

        var scriptExecution = new ScriptExecution(new ScriptExecutionInput("myId", "myScript\nsecond-line", "myInput", "myTopic"));
        Assertions.assertNull(scriptExecution.getWorkspace());

        myExecutor.execute(scriptExecution);
        Assertions.assertFalse(scriptExecution.getWorkspace().toFile().exists());
    }

    @Test
    public void testCleanWorkspaceEvenIfException() {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var myExecutor = new AbstractExecutor(config) {
            @Override
            protected String getFullScript(ScriptExecution scriptExecution) {
                return "my-header\n" + scriptExecution.getScriptExecutionInput().getScript() + "\nmy-footer\n";
            }

            @Override
            protected String getScriptName(ScriptExecution scriptExecution) {
                return "myScript.txt";
            }

            @Override
            protected int executeScript(ScriptExecution scriptExecution) throws WorkerException {
                throw new RuntimeException("oops");
            }
        };

        var scriptExecution = new ScriptExecution(new ScriptExecutionInput("myId", "myScript\nsecond-line", "myInput", "myTopic"));
        Assertions.assertNull(scriptExecution.getWorkspace());

        Assertions.assertThrows(RuntimeException.class, () -> myExecutor.execute(scriptExecution));

        Assertions.assertFalse(scriptExecution.getWorkspace().toFile().exists());
    }

    @Test
    public void testCleanWorkspaceEvenIfExceptionInSetupPhase() throws InterruptedException {
        var config = new Config();
        config.setWorkspace("/tmp/");
        config.setCleanWorkspace(true);

        var myExecutor = new AbstractExecutor(config) {
            @Override
            protected String getFullScript(ScriptExecution scriptExecution) {
                return "my-header\n" + scriptExecution.getScriptExecutionInput().getScript() + "\nmy-footer\n";
            }

            @Override
            protected String getScriptName(ScriptExecution scriptExecution) {
                throw new RuntimeException("oops");
            }

            @Override
            protected int executeScript(ScriptExecution scriptExecution) throws WorkerException {
                // not called
                return 0;
            }
        };

        var scriptExecution = new ScriptExecution(new ScriptExecutionInput("myId", "myScript\nsecond-line", "myInput", "myTopic"));
        Assertions.assertNull(scriptExecution.getWorkspace());

        Assertions.assertThrows(RuntimeException.class, () -> myExecutor.execute(scriptExecution));

        Assertions.assertFalse(scriptExecution.getWorkspace().toFile().exists());
    }

}
