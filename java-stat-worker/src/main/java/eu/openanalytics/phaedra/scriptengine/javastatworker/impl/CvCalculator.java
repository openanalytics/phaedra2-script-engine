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
package eu.openanalytics.phaedra.scriptengine.javastatworker.impl;

import eu.openanalytics.phaedra.scriptengine.javastatworker.CalculationInput;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.StatUtils;
import org.springframework.stereotype.Component;

/**
 * https://en.wikipedia.org/wiki/Coefficient_of_variation  Statistic Calculator.
 */
@Component
public class CvCalculator implements StatCalculator {

    @Override
    public String getName() {
        return "cv";
    }

    @Override
    public Double calculateForPlate(CalculationInput input) {
        return calculate(input.getPlateValues());
    }

    @Override
    public Double calculateForWelltype(CalculationInput input, String welltype, double[] values) {
        return calculate(values);
    }

    private Double calculate(double[] values) {
        var stats = StatUtils.createStats(values);
        if (stats.getMean() == 0) {
            return Double.NaN;
        }
        double value = stats.getStandardDeviation() / stats.getMean();
        value *= 100;
        return value;
    }
}
