package eu.openanalytics.phaedra.scriptengine.javastatworker;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.HashMap;

@AllArgsConstructor
@Value
public class CalculationOutput {

    Float plateValue;

    HashMap<String, Float> welltypeValues;

    public static CalculationOutputBuilder builder() {
        return new CalculationOutputBuilder();
    }

    public static class CalculationOutputBuilder {

        Float plateValue = null;

        HashMap<String, Float> welltypeValues = new HashMap<>();

        private CalculationOutputBuilder() {

        }

        public CalculationOutputBuilder plateValue(Double plateValue) {
            this.plateValue = plateValue.floatValue();
            return this;
        }

        public CalculationOutputBuilder addWelltypeValue(String wellType, Number value) {
            if (welltypeValues.containsKey(wellType)) {
                throw new IllegalArgumentException("This output already contains a value for this welltype");
            }
            if (value != null) {
                welltypeValues.put(wellType, value.floatValue());
            } else {
                welltypeValues.put(wellType, null);
            }
            return this;
        }

        public CalculationOutput build() {
            return new CalculationOutput(plateValue, welltypeValues);
        }

    }

}
