package eu.openanalytics.phaedra.scriptengine.watchdog.support;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;

import java.sql.SQLException;

public class Containers {

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer;

    @Container
    public static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management")
        .withAdminPassword(null);

    static {
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:13-alpine")
            .withUrlParam("currentSchema", "watchdog");

        postgreSQLContainer.start();
        rabbitMQContainer.start();
        try {
            var connection = postgreSQLContainer.createConnection("");
            connection.createStatement().executeUpdate("create schema watchdog");
            connection.setSchema("watchdog");

            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.yaml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());
        } catch (SQLException | LiquibaseException e) {
            e.printStackTrace();
        }
    }
}

