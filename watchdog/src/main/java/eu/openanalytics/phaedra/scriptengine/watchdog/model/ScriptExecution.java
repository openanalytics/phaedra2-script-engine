package eu.openanalytics.phaedra.scriptengine.watchdog.model;

import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Value
@Builder
public class ScriptExecution {

    @NonNull
    @Id
    String id;

    String routingKey;

    LocalDateTime queueTimestamp;

    LocalDateTime lastHeartbeat;

    ResponseStatusCode responseStatusCode;
}
