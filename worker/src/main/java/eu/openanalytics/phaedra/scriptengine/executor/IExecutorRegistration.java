package eu.openanalytics.phaedra.scriptengine.executor;

public interface IExecutorRegistration {

    String getLanguage();

    IExecutor createExecutor();

    Boolean allowConcurrency();

}
