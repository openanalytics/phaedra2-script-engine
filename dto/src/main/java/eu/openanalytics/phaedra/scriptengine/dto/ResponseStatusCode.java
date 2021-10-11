package eu.openanalytics.phaedra.scriptengine.dto;

/**
 * Enum indicating the status of a response.
 */
public enum ResponseStatusCode {
    SUCCESS,
    SCRIPT_ERROR,
    BAD_REQUEST,
    WORKER_INTERNAL_ERROR {
        @Override
        public boolean canBeRetried() {
            return true;
        }
    },
    INTERRUPTED_BY_WATCHDOG {
        @Override
        public boolean canBeRetried() {
            return true;
        }
    };

    public boolean canBeRetried() {
        return false;
    }

}
