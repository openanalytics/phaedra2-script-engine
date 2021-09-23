package eu.openanalytics.phaedra.scriptengine.javastatworker;

public interface StatCalculator {

    public String getName();

    public CalculationOutput calculate(CalculationInput calculationInput);

}
