package eu.openanalytics.phaedra.scriptengine.javastatworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutor;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutorRegistration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class JavaStatConfiguration {

    @Bean
    public IExecutorRegistration rExecutorRegistration(ObjectMapper objectMapper, List<StatCalculator> statCalculator) {
        return new IExecutorRegistration() {
            @Override
            public String getLanguage() {
                return "JavaStat";
            }

            @Override
            public IExecutor createExecutor() {
                return new JavaStatExecutor(objectMapper, statCalculator);
            }

            @Override
            public Boolean allowConcurrency() {
                return false;
            }
        };
    }

}
