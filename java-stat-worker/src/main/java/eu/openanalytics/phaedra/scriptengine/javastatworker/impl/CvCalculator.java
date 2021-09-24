package eu.openanalytics.phaedra.scriptengine.javastatworker.impl;

import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationInput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatUtils;
import org.springframework.stereotype.Component;

@Component
public class CvCalculator implements StatCalculator {

    @Override
    public String getName() {
        return "cv";
    }

    @Override
    public Double calculateForPlate(CalculationInput input) {
        return calculate(input.getPlateValues());
    }

    @Override
    public Double calculateForWelltype(CalculationInput input, String welltype, double[] values) {
        return calculate(values);
    }

    private Double calculate(double[] values) {
        var stats = StatUtils.createStats(values);
        if (stats.getMean() == 0) {
            return Double.NaN;
        }
        double value = stats.getStandardDeviation() / stats.getMean();
        value *= 100;
        return value;
    }
}
