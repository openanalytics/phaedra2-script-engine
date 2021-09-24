package eu.openanalytics.phaedra.scriptengine.javastatworker.impl;

import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationInput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatUtils;
import org.springframework.stereotype.Component;

/**
 * @see <a href="https://commons.apache.org/proper/commons-math/javadocs/api-3.3/org/apache/commons/math3/stat/descriptive/moment/Skewness.html">https://commons.apache.org/proper/commons-math/javadocs/api-3.3/org/apache/commons/math3/stat/descriptive/moment/Skewness.html</a>
 */
@Component
public class SkewnessCalculator implements StatCalculator {

    @Override
    public String getName() {
        return "skewness";
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
        return StatUtils.createStats(values).getSkewness();
    }
}
