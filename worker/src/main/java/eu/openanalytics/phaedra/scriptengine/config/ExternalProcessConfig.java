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

