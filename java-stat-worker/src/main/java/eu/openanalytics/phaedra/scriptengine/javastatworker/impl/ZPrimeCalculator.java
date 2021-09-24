package eu.openanalytics.phaedra.scriptengine.javastatworker.impl;

import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationInput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Component;

@Component
public class ZPrimeCalculator implements StatCalculator {

    @Override
    public String getName() {
        return "zprime";
    }

    @Override
    public Double calculateForPlate(CalculationInput calculationInput) {
        double[] lows = calculationInput.getLowWelltypeValues();
        double[] highs = calculationInput.getHighWelltypeValues();

        if (lows == null || lows.length == 0 || highs == null || highs.length  == 0)
            throw new IllegalStateException("TODO");

        DescriptiveStatistics lowStats = StatUtils.createStats(lows);
        DescriptiveStatistics highStats = StatUtils.createStats(highs);

        double value = 3 * (lowStats.getStandardDeviation() + highStats.getStandardDeviation());
        value = Math.abs(value);
        value = 1 - (value / Math.abs(highStats.getMean() - lowStats.getMean()));

        return StatUtils.round(value, 2);
    }

    @Override
    public Double calculateForWelltype(CalculationInput calculationInput, String welltype, double[] values) {
        return null;
    }

}
