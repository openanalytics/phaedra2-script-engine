package eu.openanalytics.phaedra.scriptengine.watchdog;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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

import javax.sql.DataSource;
import java.util.Map;

@SpringBootApplication
public class WatchdogApplication {

    private final Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(WatchdogApplication.class, args);
    }


    public final static String INPUT_EXCHANGE = "scriptengine_input";
    public final static String INPUT_QUEUE_NAME = "watchdog_input";
    public final static String OUTPUT_EXCHANGE = "scriptengine_output";
    public final static String OUTPUT_QUEUE_NAME = "watchdog_output";
    public final static String HEARTBEAT_EXCHANGE = "scriptengine_heartbeat";
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
        for (var target : watchDogConfig.getTargets()) {
            amqpAdmin.declareBinding(new Binding(INPUT_QUEUE_NAME, Binding.DestinationType.QUEUE, INPUT_EXCHANGE, target.getRoutingKey(), Map.of()));
        }
        // output exchange (-> no queues)
        amqpAdmin.declareExchange(new TopicExchange(OUTPUT_EXCHANGE, true, false));
        amqpAdmin.declareQueue(new Queue(OUTPUT_QUEUE_NAME, true, false, false));
        amqpAdmin.declareBinding(new Binding(OUTPUT_QUEUE_NAME, Binding.DestinationType.QUEUE, OUTPUT_EXCHANGE, watchDogConfig.getOutputRoutingKeyPrefix() + "*", Map.of()));


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
        String hostAndPort = environment.getProperty("DB_HOST_PORT");
        String dbName = environment.getProperty("DB_NAME");
        String schema = environment.getProperty("DB_SCHEMA", "public");
        if (StringUtils.isEmpty(hostAndPort) || StringUtils.isEmpty(dbName)) {
            throw new RuntimeException("No database host, port or name configured");
        }
        String url = String.format("jdbc:postgresql://%s/%s?currentSchema=%s", hostAndPort, dbName, schema);
        String username = environment.getProperty("DB_USER");
        String password = environment.getProperty("DB_PASSWORD");

        String driverClassName = JDBCUtils.getDriverClassName(url);
        if (driverClassName == null) {
            throw new RuntimeException("Unsupported database type: " + url);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setDriverClassName(driverClassName);
        config.setUsername(username);
        config.setPassword(password);
        config.setAutoCommit(true);

        return new HikariDataSource(config);
    }

}
