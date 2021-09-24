package eu.openanalytics.phaedra.scriptengine.javastatworker.impl;

import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationInput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Component;

/**
 * Signal/Background Statistic Calculator.
 */
@Component
public class SbCalculator implements StatCalculator {

    @Override
    public String getName() {
        return "sb";
    }

    @Override
    public Double calculateForPlate(CalculationInput input) {
        double[] lows = input.getLowWelltypeValues();
        double[] highs = input.getHighWelltypeValues();

        if (lows == null || lows.length == 0 || highs == null || highs.length == 0)
            throw new IllegalStateException("Invalid set of lows and highs");

        DescriptiveStatistics lowStats = StatUtils.createStats(lows);
        DescriptiveStatistics highStats = StatUtils.createStats(highs);

        double value = highStats.getMean() / lowStats.getMean();

       return StatUtils.round(value, 2);
    }

    @Override
    public Double calculateForWelltype(CalculationInput input, String welltype, double[] values) {
        return null;
    }

}
