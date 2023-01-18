/**
 * Phaedra II
 *
 * Copyright (C) 2016-2023 Open Analytics
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
package eu.openanalytics.phaedra.scriptengine.watchdog.model;

import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Value
@Builder
@AllArgsConstructor
public class ScriptExecution {

    @NonNull
    @Id
    String id;

    String inputRoutingKey;

    String outputRoutingKey;

    LocalDateTime queueTimestamp;

    LocalDateTime lastHeartbeat;

    ResponseStatusCode responseStatusCode;
}
