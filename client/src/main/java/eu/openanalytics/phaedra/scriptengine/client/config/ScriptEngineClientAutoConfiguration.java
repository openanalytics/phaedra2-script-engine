package eu.openanalytics.phaedra.scriptengine.client.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import eu.openanalytics.phaedra.scriptengine.client.impl.ScriptEngineClientImpl;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.ConnectionFactoryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;

@Configuration
@ConditionalOnProperty(value = "phaedra2.scriptengine.client.enabled", havingValue = "true", matchIfMissing = true)
public class ScriptEngineClientAutoConfiguration {

    @Autowired
    private ScriptEngineClientConfiguration clientConfig;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @PostConstruct
    public void init() {
        amqpAdmin.declareQueue(new Queue(clientConfig.getResponseQueueName(), true, false, false));
        amqpAdmin.declareBinding(new Binding(clientConfig.getResponseQueueName(), Binding.DestinationType.QUEUE,
                "scriptengine_output", "scriptengine.output." + clientConfig.getClientName(), Map.of()));
    }

    @Bean
    public ScriptEngineClientImpl scriptEngineClient(RabbitTemplate rabbitTemplate, ScriptEngineClientConfiguration clientConfig) {
        return new ScriptEngineClientImpl(clientConfig, rabbitTemplate);
    }

    @Bean
    public DirectMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory, RabbitTemplate rabbitTemplate, ScriptEngineClientConfiguration clientConfig) {
        DirectMessageListenerContainer container = new DirectMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addQueueNames(clientConfig.getResponseQueueName());
        container.setMessageListener(scriptEngineClient(rabbitTemplate, clientConfig));
        container.setPrefetchCount(250);
        container.setConsumersPerQueue(8);
        return container;
    }

    @Bean
    public ConnectionFactoryCustomizer connectionFactoryCustomizer() {
        return factory -> {
            var threadFactory = new ThreadFactoryBuilder().setNameFormat("rabbitmq-con-%s").build();
            factory.setThreadFactory(threadFactory);
        };
    }

}
