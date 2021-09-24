package eu.openanalytics.phaedra.scriptengine.javastatworker.impl;

import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationInput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Component;

@Component
public class SnCalculator implements StatCalculator {

    @Override
    public String getName() {
        return "sn";
    }

    @Override
    public Double calculateForPlate(CalculationInput input) {
        double[] lows = input.getLowWelltypeValues();
        double[] highs = input.getHighWelltypeValues();

        if (lows == null || lows.length == 0 || highs == null || highs.length == 0)
            throw new IllegalStateException("TODO");

        DescriptiveStatistics lowStats = StatUtils.createStats(lows);
        DescriptiveStatistics highStats = StatUtils.createStats(highs);

        double value = highStats.getMean() - lowStats.getMean();
        value = value / lowStats.getStandardDeviation();

        return StatUtils.round(value, 2);
    }

    @Override
    public Double calculateForWelltype(CalculationInput input, String welltype, double[] values) {
        return null;
    }

}
