package eu.openanalytics.phaedra.scriptengine.javastatworker.impl;

import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationInput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationOutput;
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
    public CalculationOutput calculate(CalculationInput calculationInput) {
        var outputBuilder = CalculationOutput.builder();
        for (var group : calculationInput.getGroupedValues().entrySet()) {
            var stats = StatUtils.createStats(group.getValue());
            if (stats.getMean() == 0)  {
                outputBuilder.addWelltypeValue(group.getKey(), Double.NaN);
                continue;
            }
            double value = stats.getStandardDeviation() / stats.getMean();
            value *= 100;
            outputBuilder.addWelltypeValue(group.getKey(), value);
        }
        return outputBuilder.build();
    }
}
