package eu.openanalytics.phaedra.scriptengine.javastatworker;

public interface StatCalculator {

    String getName();

    Double calculateForPlate(CalculationInput input);

    Double calculateForWelltype(CalculationInput input, String welltype, double[] values);

}
