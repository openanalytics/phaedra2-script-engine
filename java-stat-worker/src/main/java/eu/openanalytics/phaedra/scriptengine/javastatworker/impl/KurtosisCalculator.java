package eu.openanalytics.phaedra.scriptengine.javastatworker.impl;

import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationInput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatUtils;
import org.springframework.stereotype.Component;

/**
 * Kurtosis Statistic Calculator.
 * @see <a href="https://en.wikipedia.org/wiki/Kurtosis>https://en.wikipedia.org/wiki/Kurtosis</a>
 */
@Component
public class KurtosisCalculator implements StatCalculator {

    @Override
    public String getName() {
        return "kurtosis";
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
        return StatUtils.createStats(values).getKurtosis();
    }
}
