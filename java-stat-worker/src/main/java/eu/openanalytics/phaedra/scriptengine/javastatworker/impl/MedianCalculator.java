package eu.openanalytics.phaedra.scriptengine.javastatworker.impl;

import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationInput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationOutput;
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
    public CalculationOutput calculate(CalculationInput calculationInput) {
        var outputBuilder = CalculationOutput.builder();
        for (var group : calculationInput.getGroupedValues().entrySet()) {
            var median = StatUtils.percentile(group.getValue(), 50);
            outputBuilder.addWelltypeValue(group.getKey(), median);
        }
        return outputBuilder.build();
    }
}
