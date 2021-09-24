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

        public CalculationOutputBuilder roundedPateValue(double plateValue) {
            this.plateValue = (float) StatUtils.round(plateValue, 2); // TODO
            return this;
        }

        public CalculationOutputBuilder addWelltypeValue(String wellType, Double value) {
            return addWelltypeValue(wellType, value.floatValue());
        }

        public CalculationOutputBuilder addWelltypeValue(String wellType, Float value) {
            if (welltypeValues.containsKey(wellType)) {
                throw new IllegalArgumentException("TODO"); // TODO
            }
            welltypeValues.put(wellType, value); // TODO
            return this;
        }

        public CalculationOutput build() {
            return new CalculationOutput(plateValue, welltypeValues);
        }


    }

}