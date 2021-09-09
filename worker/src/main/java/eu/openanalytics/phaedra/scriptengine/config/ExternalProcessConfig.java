/**
 * ContainerProxy
 *
 * Copyright (C) 2016-2021 Open Analytics
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
package eu.openanalytics.phaedra.scriptengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;

@ConfigurationProperties(prefix = "phaedra2.script-engine-worker")
public class ExternalProcessConfig {

    private String workspace;

    private Boolean cleanWorkspace = true;

    @PostConstruct
    public void init() {
        if (workspace == null) {
            throw new IllegalArgumentException("Incorrect configuration detected: phaedra2.script-engine-worker.workspace not set");
        }
        if (!workspace.startsWith("/") || !workspace.endsWith("/")) {
            throw new IllegalArgumentException("Incorrect configuration detected: phaedra2.script-engine-worker.workspace must start and end with /");
        }
        File path = new File(workspace);
        if (!path.exists() || !path.isDirectory()) {
            throw new IllegalArgumentException("Incorrect configuration detected: phaedra2.script-engine-worker.workspace does not exists or is not a directory");
        }
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    /**
     * @return the directory that the worker must use for its temporary workspaces.
     */
    public String getWorkspace() {
        return workspace;
    }


     /**
     * @return whether the worker must clean the workspace.
     */
    public Boolean getCleanWorkspace() {
        return cleanWorkspace;
    }

    public void setCleanWorkspace(Boolean cleanWorkspace) {
        this.cleanWorkspace = cleanWorkspace;
    }


}

