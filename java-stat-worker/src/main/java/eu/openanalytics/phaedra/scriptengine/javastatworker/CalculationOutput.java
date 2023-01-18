/**
 * Phaedra II
 *
 * Copyright (C) 2016-2023 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
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
