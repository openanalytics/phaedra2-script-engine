package eu.openanalytics.phaedra.scriptengine.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE) // Jackson deserialize compatibility
@AllArgsConstructor
public class HeartbeatDTO {

    @NonNull
    String scriptExecutionId;

    String workerName;

}
