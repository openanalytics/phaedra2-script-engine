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
