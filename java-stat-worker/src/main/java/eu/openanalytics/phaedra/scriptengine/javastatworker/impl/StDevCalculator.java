package eu.openanalytics.phaedra.scriptengine.javastatworker.impl;

import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationInput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationOutput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatUtils;
import org.springframework.stereotype.Component;

@Component
public class StDevCalculator implements StatCalculator {

    @Override
    public String getName() {
        return "stdev";
    }

    @Override
    public CalculationOutput calculate(CalculationInput calculationInput) {
        var outputBuilder = CalculationOutput.builder();
        for (var group : calculationInput.getGroupedValues().entrySet()) {
            var stDev = StatUtils.createStats(group.getValue()).getStandardDeviation();
            outputBuilder.addWelltypeValue(group.getKey(), stDev);
        }
        return outputBuilder.build();
    }
}
