package eu.openanalytics.phaedra.scriptengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

@Value
@Builder
@With
public class ScriptExecutionOutputDTO {

    @NonNull
    String inputId;

    @NonNull
    String output;

    @NonNull
    ResponseStatusCode statusCode;

    @NonNull
    String statusMessage;

    @NonNull
    Integer exitCode;

    public ScriptExecutionOutputDTO(@JsonProperty(value = "inputId", required = true) @NonNull String inputId,
                                    @JsonProperty(value = "output", required = true) @NonNull String output,
                                    @JsonProperty(value = "statusCode", required = true) @NonNull ResponseStatusCode statusCode,
                                    @JsonProperty(value = "statusMessage", required = true) @NonNull String statusMessage,
                                    @JsonProperty(value = "exitCode", required = true) @NonNull Integer exitCode) {
        this.inputId = inputId;
        this.output = output;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.exitCode = exitCode;
    }



}
