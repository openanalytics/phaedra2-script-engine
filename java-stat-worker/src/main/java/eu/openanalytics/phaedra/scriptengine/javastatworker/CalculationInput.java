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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculationInput {

    private final String lowWelltype;

    private final String highWelltype;

    private final List<String> welltypes;

    private final Boolean isPlateStat;
    private final Boolean isWelltypeStat;

    // calculated/converted values
    private final double[] featureValues;
    private final Map<String, double[]> groupedValues = new HashMap<>();

    public CalculationInput(@JsonProperty(value = "lowWelltype", required = true) String lowWelltype,
                            @JsonProperty(value = "highWelltype", required = true) String highWelltype,
                            @JsonProperty(value = "welltypes", required = true) List<String> welltypes,
                            @JsonProperty(value = "isPlateStat", required = true) Boolean isPlateStat,
                            @JsonProperty(value = "isWelltypeStat", required = true) Boolean isWelltypeStat,
                            @JsonProperty(value = "featureValues", required = true) List<Float> featureValues) {
        if (welltypes.size() != featureValues.size()) {
            throw new IllegalArgumentException("Size of welltypes and featureValues does not match!");
        }

        this.lowWelltype = lowWelltype;
        this.highWelltype = highWelltype;
        this.isPlateStat = isPlateStat;
        this.isWelltypeStat = isWelltypeStat;
        this.featureValues = featureValues.stream().mapToDouble(f -> f).toArray();
        this.welltypes = welltypes;

        var welltypesIt = welltypes.iterator();
        var valuesIt = Arrays.stream(this.featureValues).iterator();

        var groupedValuesList = new HashMap<String, List<Double>>();

        while (welltypesIt.hasNext()) {
            var welltype = welltypesIt.next();
            groupedValuesList.computeIfAbsent(welltype, (k) -> new ArrayList<>())
                .add(valuesIt.next());
        }
        groupedValuesList.forEach((k, v) -> groupedValues.put(k, v.stream().mapToDouble(f -> f).toArray()));
    }

    public double[] getPlateValues() {
        return featureValues;
    }

    public List<String> getWelltypes() {
        return welltypes;
    }

    public String getHighWelltype() {
        return highWelltype;
    }

    public String getLowWelltype() {
        return lowWelltype;
    }

    public double[] getLowWelltypeValues() {
        return getValuesByWelltype(lowWelltype);
    }

    public double[] getHighWelltypeValues() {
        return getValuesByWelltype(highWelltype);
    }

    public double[] getValuesByWelltype(String welltype) {
        return groupedValues.get(welltype);
    }

    public Map<String, double[]> getValuesByWelltype() {
        return groupedValues;
    }

    public Boolean isPlateStat() {
        return isPlateStat;
    }

    public Boolean isWelltypeStat() {
        return isWelltypeStat;
    }
}
