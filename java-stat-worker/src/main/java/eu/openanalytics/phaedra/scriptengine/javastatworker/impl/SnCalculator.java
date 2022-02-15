/**
 * Phaedra II
 *
 * Copyright (C) 2016-2022 Open Analytics
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
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Component;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Signal-to-noise_statistic">https://en.wikipedia.org/wiki/Signal-to-noise_statistic</a>
 */
@Component
public class SnCalculator implements StatCalculator {

    @Override
    public String getName() {
        return "sn";
    }

    @Override
    public Double calculateForPlate(CalculationInput input) {
        double[] lows = input.getLowWelltypeValues();
        double[] highs = input.getHighWelltypeValues();

        if (lows == null || lows.length == 0 || highs == null || highs.length == 0)
            throw new IllegalStateException("Invalid set of lows and highs");

        DescriptiveStatistics lowStats = StatUtils.createStats(lows);
        DescriptiveStatistics highStats = StatUtils.createStats(highs);

        double value = highStats.getMean() - lowStats.getMean();
        value = value / lowStats.getStandardDeviation();

        return StatUtils.round(value, 2);
    }

    @Override
    public Double calculateForWelltype(CalculationInput input, String welltype, double[] values) {
        return null;
    }

}
