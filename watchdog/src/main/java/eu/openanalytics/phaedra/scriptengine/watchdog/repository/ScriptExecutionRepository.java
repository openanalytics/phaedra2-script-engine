/**
 * Phaedra II
 *
 * Copyright (C) 2016-2022 Open Analytics
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
package eu.openanalytics.phaedra.scriptengine.watchdog.repository;

import eu.openanalytics.phaedra.scriptengine.dto.HeartbeatDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.watchdog.config.WatchDogConfig;
import eu.openanalytics.phaedra.scriptengine.watchdog.model.ScriptExecution;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class ScriptExecutionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final WatchDogConfig watchDogConfig;

    public ScriptExecutionRepository(JdbcTemplate jdbcTemplate, WatchDogConfig watchDogConfig) {
        this.jdbcTemplate = jdbcTemplate;
        this.watchDogConfig = watchDogConfig;
    }

    /**
     * Creates a scriptExecution.
     */
    public void createScriptExecution(ScriptExecutionInputDTO input, String routingKey) {
        var outputRoutingKey = watchDogConfig.getOutputRoutingKeyPrefix() + input.getResponseTopicSuffix();
        if (!createSCriptExceution(input.getId(), input.getQueueTimestamp(), routingKey, outputRoutingKey)) {
            createSCriptExceution(input.getId(), input.getQueueTimestamp(), routingKey, outputRoutingKey);
        }
    }

    /**
     * Updates the heartbeat of the ScriptExecution.
     *
     * @param heartbeatDTO
     */
    public void updateScriptExecution(HeartbeatDTO heartbeatDTO) {
        if (!updateScriptExecution(heartbeatDTO.getScriptExecutionId())) {
            updateScriptExecution(heartbeatDTO.getScriptExecutionId());
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
     *
     * @param id the id of the ScriptExceution
     * @return the ScriptExecution or null
     */
    public ScriptExecution findById(String id) {
        return jdbcTemplate.queryForObject("SELECT * FROM script_execution WHERE id = ? FOR UPDATE", new RowMapper(), id);
    }

    public List<ScriptExecution> findToInterrupt(String inputRoutingKey, LocalDateTime notBefore) {
        return jdbcTemplate.query(
            "SELECT * FROM script_execution WHERE input_routing_key = ? " +
                "AND last_heartbeat < ?" +
                "AND last_heartbeat IS NOT NULL " +
                "AND response_status_code IS NULL", new RowMapper(), inputRoutingKey, notBefore);
    }

    public int deleteExpiredEntries() {
        var before = LocalDateTime.now().minusMinutes(60);
        return jdbcTemplate.update("DELETE FROM script_execution WHERE last_heartbeat < ? AND response_status_code IS NOT NULL ", before);
    }

    public List<ScriptExecution> findAll() {
        return jdbcTemplate.query("SELECT * FROM script_execution", new RowMapper());
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
    protected boolean createSCriptExceution(String id, long queuetimestmap, String inputRoutingKey, String outputRoutingKey) {
        var queuedTimestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(queuetimestmap), ZoneId.systemDefault());

        if (!existsWithLock(id)) {
            // record does not exist -> try to create it
            try {
                jdbcTemplate.update("INSERT INTO script_execution (id, queue_timestamp, input_routing_key, output_routing_key) VALUES (?, ?, ?, ?)", id, queuedTimestamp, inputRoutingKey, outputRoutingKey);
            } catch (DuplicateKeyException e) {
                // record was created in the meantime
                return false;
            }
        } else {
            // record exists and locked -> update it
            jdbcTemplate.update("UPDATE script_execution set queue_timestamp = ?, input_routing_key = ?, output_routing_key = ? WHERE id =?", queuedTimestamp, inputRoutingKey, outputRoutingKey, id);
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
    protected boolean updateScriptExecution(String id) {
        var lastHeartbeat = LocalDateTime.now();

        if (!existsWithLock(id)) {
            try {
                jdbcTemplate.update("INSERT INTO script_execution (id, last_heartbeat) VALUES (?, ?)", id, lastHeartbeat);
            } catch (DuplicateKeyException ex) {
                // record was created in the meantime
                return false;
            }
        } else {
            jdbcTemplate.update("UPDATE script_execution set last_heartbeat = ? WHERE id =?", lastHeartbeat, id);
        }
        return true;
    }

    /**
     * Creates or update a script execution with:
     * - id
     * - response_status_code
     *
     * @param id         id of the ScriptExecution
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
            var res = ScriptExecution.builder()
                .id(rs.getString("ID"))
                .lastHeartbeat(rs.getObject("last_heartbeat", LocalDateTime.class))
                .queueTimestamp(rs.getObject("queue_timestamp", LocalDateTime.class))
                .inputRoutingKey(rs.getString("input_routing_key"))
                .outputRoutingKey(rs.getString("output_routing_key"));

            if (rs.getString("response_status_code") != null) {
                res.responseStatusCode(ResponseStatusCode.valueOf(rs.getString("response_status_code")));
            }

            return res.build();
        }
    }

}
