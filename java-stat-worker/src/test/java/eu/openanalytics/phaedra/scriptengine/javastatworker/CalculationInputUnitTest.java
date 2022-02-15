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
package eu.openanalytics.phaedra.scriptengine.javastatworker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CalculationInputUnitTest {

    @Test
    public void basicTest() {
        var input = new CalculationInput("LC", "HC",
            List.of("LC", "SAMPLE", "SAMPLE", "HC", "LC", "SAMPLE", "SAMPLE", "HC", "LC", "SAMPLE", "SAMPLE", "HC"),
            true,
            false,
            List.of(
                // LC       SAMPLE      SAMPLE      HC
                95.451744f, 62.479553f, 77.93638f, 77.093376f,
                89.971886f, 33.307343f, 99.33542f, 74.45653f,
                66.75077f, 44.107273f, 52.945477f, 12.092348f
            )
        );
        Assertions.assertEquals("LC", input.getLowWelltype());
        Assertions.assertEquals("HC", input.getHighWelltype());
        Assertions.assertArrayEquals(new double[]{
            95.45174407958984, 62.47955322265625, 77.9363784790039, 77.09337615966797,
            89.97188568115234, 33.307342529296875, 99.33541870117188, 74.45652770996094,
            66.75077056884766, 44.10727310180664, 52.94547653198242, 12.092348098754883}, input.getPlateValues());
        Assertions.assertArrayEquals(new double[]{77.09337615966797, 74.45652770996094, 12.092348098754883}, input.getHighWelltypeValues());
        Assertions.assertArrayEquals(new double[]{95.45174407958984, 89.97188568115234, 66.75077056884766}, input.getLowWelltypeValues());
        Assertions.assertArrayEquals(new double[]{62.47955322265625, 77.9363784790039, 33.307342529296875, 99.33541870117188, 44.10727310180664, 52.94547653198242}, input.getValuesByWelltype("SAMPLE"));
        Assertions.assertNull(input.getValuesByWelltype("BOGUS"));
        Assertions.assertEquals(true, input.isPlateStat());
        Assertions.assertEquals(false, input.isWelltypeStat());
        Assertions.assertEquals(List.of("LC", "SAMPLE", "SAMPLE", "HC", "LC", "SAMPLE", "SAMPLE", "HC", "LC", "SAMPLE", "SAMPLE", "HC"), input.getWelltypes());
    }

    @Test
    public void amountOfWelltypesDoesNotMatchFeatureValues() {
        var ex = Assertions.assertThrows(IllegalArgumentException.class,
            () -> new CalculationInput("LC", "HC", List.of("LC"), true, false, List.of(95.451744f, 62.479553f)));
        Assertions.assertEquals("Size of welltypes and featureValues does not match!", ex.getMessage());

    }
}
