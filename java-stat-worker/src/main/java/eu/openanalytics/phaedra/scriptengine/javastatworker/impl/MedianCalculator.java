package eu.openanalytics.phaedra.scriptengine.javastatworker.impl;

import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationInput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatCalculator;
import org.apache.commons.math.stat.StatUtils;
import org.springframework.stereotype.Component;

@Component
public class MedianCalculator implements StatCalculator {

    @Override
    public String getName() {
        return "median";
    }

    @Override
    public Double calculateForPlate(CalculationInput input) {
        return calculate(input.getPlateValues());
    }

    @Override
    public Double calculateForWelltype(CalculationInput input, String welltype, double[] values) {
        return calculate(values);
    }

    private double calculate(double[] values) {
        return StatUtils.percentile(values, 50);
    }
}
