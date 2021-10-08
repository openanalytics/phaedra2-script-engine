package eu.openanalytics.phaedra.scriptengine.watchdog;

import eu.openanalytics.phaedra.scriptengine.watchdog.config.WatchDogConfig;
import eu.openanalytics.phaedra.scriptengine.watchdog.service.MessageListenerService;
import eu.openanalytics.phaedra.util.jdbc.JDBCUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Map;

@SpringBootApplication
public class WatchdogApplication {

    private final Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(WatchdogApplication.class, args);
    }


    private final static String INPUT_EXCHANGE = "scriptengine_input";
    public final static String INPUT_QUEUE_NAME = "watchdog_input";
    private final static String OUTPUT_EXCHANGE = "scriptengine_output";
    public final static String OUTPUT_QUEUE_NAME = "watchdog_output";
    private final static String HEARTBEAT_EXCHANGE = "scriptengine_heartbeat";
    public final static String HEARTBEAT_QUEUE_NAME = "watchdog_heartbeat";

    public WatchdogApplication(AmqpAdmin amqpAdmin, WatchDogConfig watchDogConfig, Environment environment) {
//        this.envConfig = envConfig;
//        inputQueueName = envConfig.getInputQueueName();

        // input exchange and queues

//        logger.info("Using {} as name for the input exchange", inputExchangeName);
//        logger.info("Using {} as name for the input queue", inputQueueName);
//        logger.info("Using {} as routing key for the input queue", inputQueueName);
//        logger.info("Using {} as name for the output exchange", outputExchangeName);
//        logger.info("Using {} as routing key prefix for the output exchange", envConfig.getOutputRoutingKeyPrefix());
        this.environment = environment;
    }

    // TODO thread factory name?

    @Bean
    public DirectMessageListenerContainer messageListenerContainer(AmqpAdmin amqpAdmin, ConnectionFactory connectionFactory, WatchDogConfig watchDogConfig, MessageListenerService messageListenerService) {
        // TODO fix this
        amqpAdmin.declareExchange(new DirectExchange(INPUT_EXCHANGE, true, false));
        amqpAdmin.declareQueue(new Queue(INPUT_QUEUE_NAME, true, false, false));
        for (var workerQueue : watchDogConfig.getInputQueues()) {
            amqpAdmin.declareBinding(new Binding(INPUT_QUEUE_NAME, Binding.DestinationType.QUEUE, INPUT_EXCHANGE, workerQueue.getRoutingKey(), Map.of()));
        }
        // output exchange (-> no queues)
        amqpAdmin.declareExchange(new TopicExchange(OUTPUT_EXCHANGE, true, false));
        amqpAdmin.declareQueue(new Queue(OUTPUT_QUEUE_NAME, true, false, false));
        amqpAdmin.declareBinding(new Binding(OUTPUT_QUEUE_NAME, Binding.DestinationType.QUEUE, OUTPUT_EXCHANGE, watchDogConfig.getOutputRoutingKey(), Map.of()));


        amqpAdmin.declareExchange(new DirectExchange(HEARTBEAT_EXCHANGE, true, false));
        // TODO make it exclusive?
        amqpAdmin.declareQueue(new Queue(HEARTBEAT_QUEUE_NAME, true, false, false));
        amqpAdmin.declareBinding(new Binding(HEARTBEAT_QUEUE_NAME, Binding.DestinationType.QUEUE, HEARTBEAT_EXCHANGE, "heartbeat", Map.of()));

        var container = new DirectMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addQueueNames(INPUT_QUEUE_NAME, OUTPUT_QUEUE_NAME, HEARTBEAT_QUEUE_NAME);
        container.setMessageListener(messageListenerService);
        container.setPrefetchCount(250);
        container.setConsumersPerQueue(8);
        return container;
    }


    @Bean
    public DataSource dataSource() {
//        String url = "jdbc:postgresql://localhost:5432/phaedra2";
//        String username = "phaedra2";
//        String password = "phaedra2";
//        String schema = "watchdog";
        String url = environment.getProperty("DB_URL");
        String username = environment.getProperty("DB_USER");
        String password = environment.getProperty("DB_PASSWORD");
        String schema = environment.getProperty("DB_SCHEMA");

        if (StringUtils.isEmpty(url)) {
            throw new RuntimeException("No database URL configured: " + url);
        }
        String driverClassName = JDBCUtils.getDriverClassName(url);
        if (driverClassName == null) {
            throw new RuntimeException("Unsupported database type: " + url);
        }

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        if (!StringUtils.isEmpty(schema)) {
            dataSource.setSchema(schema);
        }
        return dataSource;
    }

}
