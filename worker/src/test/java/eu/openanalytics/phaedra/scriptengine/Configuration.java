package eu.openanalytics.phaedra.scriptengine;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutor;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutorRegistration;
import org.springframework.context.annotation.Bean;

public class Configuration {

    @Bean
    public IExecutorRegistration noopExecutorRegistration() {
        return new IExecutorRegistration() {
            @Override
            public String getLanguage() {
                return "noop";
            }

            @Override
            public IExecutor createExecutor() {
                return new IExecutor() {
                    @Override
                    public ScriptExecutionOutputDTO execute(ScriptExecutionInputDTO scriptExecution) throws InterruptedException, JsonProcessingException {
                        return new ScriptExecutionOutputDTO(scriptExecution.getId(), "noop-output", ResponseStatusCode.SUCCESS, "Ok", 0);
                    }
                };
            }

            @Override
            public Boolean allowConcurrency() {
                return false;
            }
        };
    }
}
