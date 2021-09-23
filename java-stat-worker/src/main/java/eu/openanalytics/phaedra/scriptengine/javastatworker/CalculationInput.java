package eu.openanalytics.phaedra.scriptengine.javastatworker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculationInput {

    private final String lowWelltype;

    private final String highWelltype;

    private final double[] featureValues;
    private final List<String> welltypes;

    private final Map<String, double[]> groupedValues = new HashMap<>();

    public CalculationInput(String lowWelltype, String highWelltype, List<String> welltypes, List<Float> featureValues) {
        if (welltypes.size() != featureValues.size()) {
            throw new IllegalArgumentException("TODO");
        }

        this.lowWelltype = lowWelltype;
        this.highWelltype = highWelltype;
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

    public double[] getFeatureValues() {
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

    public double[] getLowWellFeatures() {
        return getFeaturesOfWelltype(lowWelltype);
    }

    public double[] getHighWellFeatures() {
        return getFeaturesOfWelltype(highWelltype);
    }

    public double[] getFeaturesOfWelltype(String welltype) {
        return groupedValues.get(welltype);
    }

    public Map<String, double[]> getGroupedValues() {
        return groupedValues;
    }

}
