package eu.openanalytics.phaedra.scriptengine.javastatworker.impl;

import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationInput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationOutput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatCalculator;
import org.springframework.stereotype.Component;

@Component
public class CountCalculator implements StatCalculator {

    @Override
    public String getName() {
        return "count";
    }

    @Override
    public CalculationOutput calculate(CalculationInput calculationInput) {
        var outputBuilder = CalculationOutput.builder();
        for (var group : calculationInput.getGroupedValues().entrySet()) {
            outputBuilder.addWelltypeValue(group.getKey(), (float) group.getValue().length);
        }
        return outputBuilder.build();
    }
}
