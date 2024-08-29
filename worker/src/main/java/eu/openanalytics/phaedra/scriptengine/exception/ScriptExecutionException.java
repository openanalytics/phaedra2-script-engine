package eu.openanalytics.phaedra.scriptengine.exception;

public class ScriptExecutionException extends Exception {

	private static final long serialVersionUID = -4508504997112377828L;

	public ScriptExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ScriptExecutionException(String msg) {
        super(msg);
    }

}
