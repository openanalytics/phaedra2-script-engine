package eu.openanalytics.phaedra.scriptengine.javastatworker;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StatUtils {

    public static DescriptiveStatistics createStats(double[] values) {
        return new DescriptiveStatistics(values);
    }

    public static double round(double value, int decimals) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return value;
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        value = bd.doubleValue();
        return value;
    }

}
