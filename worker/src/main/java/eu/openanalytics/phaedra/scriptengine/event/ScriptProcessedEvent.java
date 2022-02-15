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
package eu.openanalytics.phaedra.scriptengine.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event when Script has been fully processed (i.e. executed).
 */
public class ScriptProcessedEvent extends ApplicationEvent {

    private final String scriptExecutionId;

    public ScriptProcessedEvent(Object source, String scriptExecutionId) {
        super(source);
        this.scriptExecutionId = scriptExecutionId;
    }

    public String getScriptExecutionId() {
        return scriptExecutionId;
    }
}
