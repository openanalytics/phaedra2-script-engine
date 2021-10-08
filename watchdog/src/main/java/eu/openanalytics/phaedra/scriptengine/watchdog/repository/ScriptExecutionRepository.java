package eu.openanalytics.phaedra.scriptengine.watchdog.repository;

import eu.openanalytics.phaedra.scriptengine.dto.HeartbeatDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.watchdog.model.ScriptExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class ScriptExecutionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ScriptExecutionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Creates a scriptExecution.
     */
    public void createWatch(ScriptExecutionInputDTO input, String routingKey) {
        if (!_createWatch(input.getId(), input.getQueueTimestamp(), routingKey)) {
            _createWatch(input.getId(), input.getQueueTimestamp(), routingKey);
        }
    }

    /**
     * Updates the heartbeat of the ScriptExecution.
     *
     * @param heartbeatDTO
     */
    public void updateScriptExecution(HeartbeatDTO heartbeatDTO) {
        if (!_updateWatch(heartbeatDTO.getScriptExecutionId())) {
            _updateWatch(heartbeatDTO.getScriptExecutionId());
        }
    }

    /**
     * Stops the heartbeat of the ScriptExecution.
     *
     * @param output
     */
    public void stopScriptExecution(ScriptExecutionOutputDTO output) {
        if (!stopScriptExecution(output.getInputId(), output.getStatusCode())) {
            stopScriptExecution(output.getInputId(), output.getStatusCode());
        }
    }

    /**
     * Find a ScriptExecution by id.
     * @param id the id of the ScriptExceution
     * @return the ScriptExecution or null
     */
    public ScriptExecution findById(String id) {
        return jdbcTemplate.queryForObject("SELECT * FROM script_execution WHERE id = ? FOR UPDATE", new RowMapper(), id);
    }


    /**
     * Checks whether a ScriptExecution for the given id exists.
     * If it exists it locks the row in the database using the `FOR UPDATE` option.
     *
     * @param id the id to check
     * @return whether the record exists
     * @see <a href="https://www.postgresql.org/docs/9.0/sql-select.html#SQL-FOR-UPDATE-SHARE">PostgreSQL docs</a>
     */
    private Boolean existsWithLock(String id) {
        return jdbcTemplate.query("SELECT id FROM script_execution WHERE id = ? FOR UPDATE", ResultSet::next, id);
    }

    /**
     * Creates or updates a ScriptExecution record with:
     * - id
     * - queue_timestamp
     * - routing_key
     *
     * @return whether the record was successfully created/updated. May be false when the record was created while calling this function.
     */
    @Transactional
    protected boolean _createWatch(String id, long queuetimestmap, String routingKey) {
        var queuedTimestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(queuetimestmap), ZoneId.systemDefault());

        if (!existsWithLock(id)) {
            // record does not exist -> try to create it
            try {
                jdbcTemplate.update("INSERT INTO script_execution (id, queue_timestamp, routing_key) VALUES (?, ?, ?)", id, queuedTimestamp, routingKey);
            } catch (DuplicateKeyException e) {
                // record was created in the meantime
                logger.warn("Id: {} failed to create (INSERT failed)", id);
                return false;
            }
        } else {
            // record exists and locked -> update it
            jdbcTemplate.update("UPDATE script_execution set queue_timestamp = ?, routing_key = ? WHERE id =?", queuedTimestamp, routingKey, id);
        }
        return true;
    }


    /**
     * Creates or update a script execution with:
     * - id
     * - last_heartbeat
     *
     * @param id id of the ScriptExecution
     * @return whether the record was successfully created/updated. May be false when the record was created while calling this function.
     */
    @Transactional
    protected boolean _updateWatch(String id) {
        var lastHeartbeat = LocalDateTime.now();

        if (!existsWithLock(id)) {
            try {
                jdbcTemplate.update("INSERT INTO script_execution (id, last_heartbeat) VALUES (?, ?)", id, lastHeartbeat);
            } catch (DuplicateKeyException ex) {
                // record was created in the meantime
                logger.warn("Id: {} failed to update (INSERT failed)", id);
                return false;
            }
        } else {
            jdbcTemplate.update("UPDATE script_execution set last_heartbeat = ? WHERE id =?", lastHeartbeat, id);
        }
        return true;
    }

    /**
     * Creates or update a script execution with:
     *  - id
     *  - response_status_code
     *
     * @param id id of the ScriptExecution
     * @param statusCode the statusCode
     * @return whether the record was successfully created/updated. May be false when the record was created while calling this function.
     */
    @Transactional
    protected boolean stopScriptExecution(String id, ResponseStatusCode statusCode) {
        if (!existsWithLock(id)) {
            try {
                jdbcTemplate.update("INSERT INTO script_execution (id, response_status_code) VALUES (?, ?)", id, statusCode.toString());
            } catch (DuplicateKeyException ex) {
                // record was created in the meantime
                logger.warn("Id: {} failed to stop (INSERT failed)", id);
                return false;
            }
        } else {
            jdbcTemplate.update("UPDATE script_execution set response_status_code = ? WHERE id =?", statusCode.toString(), id);
        }
        return true;
    }


    private static class RowMapper implements org.springframework.jdbc.core.RowMapper<ScriptExecution> {

        @Override
        public ScriptExecution mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ScriptExecution.builder()
                .id(rs.getString("ID"))
                .lastHeartbeat(rs.getObject("last_heartbeat", LocalDateTime.class))
                .queueTimestamp(rs.getObject("queue_timestamp", LocalDateTime.class))
                .routingKey(rs.getString("routing_key"))
                .responseStatusCode(ResponseStatusCode.valueOf(rs.getString("response_status_code")))
                .build();

        }
    }

}