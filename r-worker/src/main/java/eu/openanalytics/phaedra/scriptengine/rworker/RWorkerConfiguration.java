package eu.openanalytics.phaedra.scriptengine.rworker;

import eu.openanalytics.phaedra.scriptengine.config.ExternalProcessConfig;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutorRegistration;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RWorkerConfiguration {

    @Bean
    public IExecutorRegistration rExecutorRegistration() {
        return new IExecutorRegistration() {
            @Override
            public String getLanguage() {
                return "R";
            }

            @Override
            public IExecutor createExecutor() {
                return new RExecutor(externalProcessConfig());
            }

            public Boolean allowConcurrency() {
                return false;
            }
        };
    }

    @Bean
    public ExternalProcessConfig externalProcessConfig() {
        return new ExternalProcessConfig();
    }

}
