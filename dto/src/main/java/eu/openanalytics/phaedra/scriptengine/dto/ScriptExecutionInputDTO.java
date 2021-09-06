package eu.openanalytics.phaedra.scriptengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * POJO holding all information that is part of the request to execute a script.
 */
@Value
@Builder
@With
public class ScriptExecutionInputDTO {

    String id;
    String script;
    String input;
    String responseTopicSuffix;
    long queueTimestamp;

    public ScriptExecutionInputDTO(@JsonProperty(value = "id", required = true) String id,
                                   @JsonProperty(value = "script", required = true) String script,
                                   @JsonProperty(value = "input", required = true) String input,
                                   @JsonProperty(value = "responseTopicSuffix", required = true) String responseTopicSuffix,
                                   @JsonProperty(value = "queueTimestamp", required = true) long queueTimestamp) {
        this.id = id;
        this.script = script;
        this.input = input;
        this.responseTopicSuffix = responseTopicSuffix;
        this.queueTimestamp = queueTimestamp;
    }

}

