package eu.openanalytics.phaedra.scriptengine.javastatworker.impl;

import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationInput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationOutput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatCalculator;
import org.apache.commons.math.stat.StatUtils;
import org.springframework.stereotype.Component;

@Component
public class MeanCalculator implements StatCalculator {

    @Override
    public String getName() {
        return "mean";
    }

    @Override
    public CalculationOutput calculate(CalculationInput calculationInput) {
        var outputBuilder = CalculationOutput.builder();
        for (var group : calculationInput.getGroupedValues().entrySet()) {
            var mean = StatUtils.mean(group.getValue());
            outputBuilder.addWelltypeValue(group.getKey(), mean);
        }
        return outputBuilder.build();
    }
}
