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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.javastatworker.impl.CountCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.impl.CvCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.impl.KurtosisCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.impl.MaxCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.impl.MeanCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.impl.MedianCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.impl.MinCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.impl.SbCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.impl.SkewnessCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.impl.SnCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.impl.StDevCalculator;
import eu.openanalytics.phaedra.scriptengine.javastatworker.impl.ZPrimeCalculator;

//@Testcontainers
//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = {ScriptEngineWorkerApplication.class})
//@TestPropertySource(locations = "classpath:application-test.properties")
public class JavaStatExecutorTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    // test parsing of input

    @Test
    public void basicTest() throws JsonProcessingException {
        List<StatCalculator> calculators = List.of(new CountCalculator());
        var javaStatExecutor = new JavaStatExecutor(objectMapper, calculators);

        var input = ScriptExecutionInputDTO.builder()
            .id("myId")
            .input(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
                put("lowWelltype", "LC");
                put("highWelltype", "HC");
                put("welltypes", List.of("LC", "SAMPLE", "SAMPLE", "HC", "LC", "SAMPLE", "SAMPLE", "HC", "LC", "SAMPLE", "SAMPLE", "HC"));
                put("featureValues", List.of(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1));
                put("isPlateStat", true);
                put("isWelltypeStat", true);
            }}))
            .script("JavaStat::count")
            .build();
        var output = javaStatExecutor.execute(input);

        Assertions.assertEquals("{\"plateValue\":12.0,\"welltypeValues\":{\"SAMPLE\":6.0,\"LC\":3.0,\"HC\":3.0}}", output.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output.getStatusCode());
        Assertions.assertEquals("Ok", output.getStatusMessage());
        Assertions.assertEquals(0, output.getExitCode());
        Assertions.assertEquals("myId", output.getInputId());
    }

    @Test
    public void zPrimeTest() throws JsonProcessingException {
        var output1 = calculate(new ZPrimeCalculator(), true, true);
        // if we ask to calculate ZPrime for wellTypes -> expect it to return null
        Assertions.assertEquals("{\"plateValue\":-4.29,\"welltypeValues\":{\"SAMPLE\":null,\"LC\":null,\"HC\":null}}", output1.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output1.getStatusCode());
        Assertions.assertEquals("Ok", output1.getStatusMessage());
        Assertions.assertEquals(0, output1.getExitCode());
        Assertions.assertEquals("myId", output1.getInputId());

        var output2 = calculate(new ZPrimeCalculator(), true, false);
        // if we ask to calculate ZPrime only for plate -> do not expect it to return any value for the welltypes
        Assertions.assertEquals("{\"plateValue\":-4.29,\"welltypeValues\":{}}", output2.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output2.getStatusCode());
        Assertions.assertEquals("Ok", output2.getStatusMessage());
        Assertions.assertEquals(0, output2.getExitCode());
        Assertions.assertEquals("myId", output2.getInputId());

        // 3. test invalid lows/highs
        var output3 = calculateWithInvalidLowsAndHighs(new ZPrimeCalculator(), true, false);
        Assertions.assertEquals("{\"plateValue\":null,\"welltypeValues\":{}}", output3.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SCRIPT_ERROR, output3.getStatusCode());
        Assertions.assertEquals("Exception during execution of stat", output3.getStatusMessage());
        Assertions.assertEquals(0, output3.getExitCode());
        Assertions.assertEquals("myId", output3.getInputId());
    }

    @Test
    public void stDevTest() throws JsonProcessingException {
        var output1 = calculate(new StDevCalculator(), true, true);
        Assertions.assertEquals("{\"plateValue\":26.154787,\"welltypeValues\":{\"SAMPLE\":23.970564,\"LC\":15.236988,\"HC\":36.7908}}", output1.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output1.getStatusCode());
        Assertions.assertEquals("Ok", output1.getStatusMessage());
        Assertions.assertEquals(0, output1.getExitCode());
        Assertions.assertEquals("myId", output1.getInputId());
    }

    @Test
    public void snTest() throws JsonProcessingException {
        var output1 = calculate(new SnCalculator(), true, true);
        Assertions.assertEquals("{\"plateValue\":-1.94,\"welltypeValues\":{\"SAMPLE\":null,\"LC\":null,\"HC\":null}}", output1.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output1.getStatusCode());
        Assertions.assertEquals("Ok", output1.getStatusMessage());
        Assertions.assertEquals(0, output1.getExitCode());
        Assertions.assertEquals("myId", output1.getInputId());

        var output2 = calculate(new SnCalculator(), true, false);
        // if we ask to calculate ZPrime only for sn-> do not expect it to return any value for the welltypes
        Assertions.assertEquals("{\"plateValue\":-1.94,\"welltypeValues\":{}}", output2.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output2.getStatusCode());
        Assertions.assertEquals("Ok", output2.getStatusMessage());
        Assertions.assertEquals(0, output2.getExitCode());
        Assertions.assertEquals("myId", output2.getInputId());

        // 3. test invalid lows/highs
        var output3 = calculateWithInvalidLowsAndHighs(new SnCalculator(), true, false);
        Assertions.assertEquals("{\"plateValue\":null,\"welltypeValues\":{}}", output3.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SCRIPT_ERROR, output3.getStatusCode());
        Assertions.assertEquals("Exception during execution of stat", output3.getStatusMessage());
        Assertions.assertEquals(0, output3.getExitCode());
        Assertions.assertEquals("myId", output3.getInputId());
    }

    @Test
    public void skewnessTest() throws JsonProcessingException {
        var output1 = calculate(new SkewnessCalculator(), true, true);
        Assertions.assertEquals("{\"plateValue\":-0.6883814,\"welltypeValues\":{\"SAMPLE\":0.6253696,\"LC\":-1.4834417,\"HC\":-1.7220469}}", output1.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output1.getStatusCode());
        Assertions.assertEquals("Ok", output1.getStatusMessage());
        Assertions.assertEquals(0, output1.getExitCode());
        Assertions.assertEquals("myId", output1.getInputId());
    }

    @Test
    public void sbTest() throws JsonProcessingException {
        var output1 = calculate(new SbCalculator(), true, true);
        Assertions.assertEquals("{\"plateValue\":0.65,\"welltypeValues\":{\"SAMPLE\":null,\"LC\":null,\"HC\":null}}", output1.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output1.getStatusCode());
        Assertions.assertEquals("Ok", output1.getStatusMessage());
        Assertions.assertEquals(0, output1.getExitCode());
        Assertions.assertEquals("myId", output1.getInputId());

        var output2 = calculate(new SbCalculator(), true, false);
        // if we ask to calculate ZPrime only for sn-> do not expect it to return any value for the welltypes
        Assertions.assertEquals("{\"plateValue\":0.65,\"welltypeValues\":{}}", output2.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output2.getStatusCode());
        Assertions.assertEquals("Ok", output2.getStatusMessage());
        Assertions.assertEquals(0, output2.getExitCode());
        Assertions.assertEquals("myId", output2.getInputId());

        // 3. test invalid lows/highs
        var output3 = calculateWithInvalidLowsAndHighs(new SbCalculator(), true, false);
        Assertions.assertEquals("{\"plateValue\":null,\"welltypeValues\":{}}", output3.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SCRIPT_ERROR, output3.getStatusCode());
        Assertions.assertEquals("Exception during execution of stat", output3.getStatusMessage());
        Assertions.assertEquals(0, output3.getExitCode());
        Assertions.assertEquals("myId", output3.getInputId());
    }

    @Test
    public void minTest() throws JsonProcessingException {
        var output1 = calculate(new MinCalculator(), true, true);
        Assertions.assertEquals("{\"plateValue\":12.092348,\"welltypeValues\":{\"SAMPLE\":33.307343,\"LC\":66.75077,\"HC\":12.092348}}", output1.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output1.getStatusCode());
        Assertions.assertEquals("Ok", output1.getStatusMessage());
        Assertions.assertEquals(0, output1.getExitCode());
        Assertions.assertEquals("myId", output1.getInputId());
    }

    @Test
    public void medianTest() throws JsonProcessingException {
        var output1 = calculate(new MedianCalculator(), true, true);
        Assertions.assertEquals("{\"plateValue\":70.60365,\"welltypeValues\":{\"SAMPLE\":57.712517,\"LC\":89.971886,\"HC\":74.45653}}", output1.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output1.getStatusCode());
        Assertions.assertEquals("Ok", output1.getStatusMessage());
        Assertions.assertEquals(0, output1.getExitCode());
        Assertions.assertEquals("myId", output1.getInputId());
    }

    @Test
    public void meanTest() throws JsonProcessingException {
        var output1 = calculate(new MeanCalculator(), true, true);
        Assertions.assertEquals("{\"plateValue\":65.49401,\"welltypeValues\":{\"SAMPLE\":61.68524,\"LC\":84.058136,\"HC\":54.547417}}", output1.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output1.getStatusCode());
        Assertions.assertEquals("Ok", output1.getStatusMessage());
        Assertions.assertEquals(0, output1.getExitCode());
        Assertions.assertEquals("myId", output1.getInputId());
    }

    @Test
    public void maxTest() throws JsonProcessingException {
        var output1 = calculate(new MaxCalculator(), true, true);
        Assertions.assertEquals("{\"plateValue\":99.33542,\"welltypeValues\":{\"SAMPLE\":99.33542,\"LC\":95.451744,\"HC\":77.093376}}", output1.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output1.getStatusCode());
        Assertions.assertEquals("Ok", output1.getStatusMessage());
        Assertions.assertEquals(0, output1.getExitCode());
        Assertions.assertEquals("myId", output1.getInputId());
    }

    @Test
    public void kurtosisTest() throws JsonProcessingException {
        var output1 = calculate(new KurtosisCalculator(), true, true);
        // NaN -> expected since size of LC and HC is 3
        Assertions.assertEquals("{\"plateValue\":-0.004569263,\"welltypeValues\":{\"SAMPLE\":-0.25188518,\"LC\":\"NaN\",\"HC\":\"NaN\"}}", output1.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output1.getStatusCode());
        Assertions.assertEquals("Ok", output1.getStatusMessage());
        Assertions.assertEquals(0, output1.getExitCode());
        Assertions.assertEquals("myId", output1.getInputId());
    }

    @Test
    public void cvTest() throws JsonProcessingException {
        var output1 = calculate(new CvCalculator(), true, true);
        Assertions.assertEquals("{\"plateValue\":39.934624,\"welltypeValues\":{\"SAMPLE\":38.85948,\"LC\":18.126728,\"HC\":67.44737}}", output1.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output1.getStatusCode());
        Assertions.assertEquals("Ok", output1.getStatusMessage());
        Assertions.assertEquals(0, output1.getExitCode());
        Assertions.assertEquals("myId", output1.getInputId());

        // test with mean of zero
        var output2 = calculate(new CvCalculator(), true, true, List.of(
            // LC  SAMPLE SAMPLE HC
            -3.0, -2.0, 2.0, 3.0,
            0.0, 0.0, 0.0, 0.0,
            3.0, 2.0, -2.0, -3.0
        ));
        Assertions.assertEquals("{\"plateValue\":\"NaN\",\"welltypeValues\":{\"SAMPLE\":\"NaN\",\"LC\":\"NaN\",\"HC\":\"NaN\"}}", output2.getOutput());
        Assertions.assertEquals(ResponseStatusCode.SUCCESS, output2.getStatusCode());
        Assertions.assertEquals("Ok", output2.getStatusMessage());
        Assertions.assertEquals(0, output2.getExitCode());
        Assertions.assertEquals("myId", output2.getInputId());
    }

    /**
     * Helper to easily create an integration test for a StatCalculator.
     */
    private ScriptExecutionOutputDTO calculate(StatCalculator statCalculator, boolean isPlateStat, boolean isWelltypeStat) throws JsonProcessingException {
        /* R scripts
           values <- c(95.45174575424718, 62.47955391280049, 77.93638152056002, 77.09337725442568, 89.97188793489511, 33.3073435903634, 99.33541818858255, 74.45653080903729, 66.75076984121507, 44.10727370863094, 52.94547610582584, 12.092348348947057)
           lc <- c(95.45174575424718, 89.97188793489511, 66.75076984121507)
           hc <- c(77.09337725442568, 74.45653080903729, 12.092348348947057)
         */
        return calculate(statCalculator, isPlateStat, isWelltypeStat, List.of(
            // LC              SAMPLE             SAMPLE             HC
            95.45174575424718, 62.47955391280049, 77.93638152056002, 77.09337725442568,
            89.97188793489511, 33.3073435903634, 99.33541818858255, 74.45653080903729,
            66.75076984121507, 44.10727370863094, 52.94547610582584, 12.092348348947057));
    }

    private ScriptExecutionOutputDTO calculate(StatCalculator statCalculator, boolean isPlateStat, boolean isWelltypeStat, List<Double> values) throws JsonProcessingException {
        var calculators = List.of(statCalculator);
        var javaStatExecutor = new JavaStatExecutor(objectMapper, calculators);

        var input = ScriptExecutionInputDTO.builder()
            .id("myId")
            .input(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
                put("lowWelltype", "LC");
                put("highWelltype", "HC");
                put("welltypes", List.of("LC", "SAMPLE", "SAMPLE", "HC", "LC", "SAMPLE", "SAMPLE", "HC", "LC", "SAMPLE", "SAMPLE", "HC"));
                put("featureValues", values);
                put("isPlateStat", isPlateStat);
                put("isWelltypeStat", isWelltypeStat);
            }}))
            .script("JavaStat::" + statCalculator.getName())
            .build();
        return javaStatExecutor.execute(input);
    }

    private ScriptExecutionOutputDTO calculateWithInvalidLowsAndHighs(StatCalculator statCalculator, boolean isPlateStat, boolean isWelltypeStat) throws JsonProcessingException {
        var calculators = List.of(statCalculator);
        var javaStatExecutor = new JavaStatExecutor(objectMapper, calculators);

        var input = ScriptExecutionInputDTO.builder()
            .id("myId")
            .input(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
                put("lowWelltype", "InvalidValue");
                put("highWelltype", "InvalidValue");
                put("welltypes", List.of("LC", "SAMPLE", "SAMPLE", "HC", "LC", "SAMPLE", "SAMPLE", "HC", "LC", "SAMPLE", "SAMPLE", "HC"));
                put("featureValues", List.of(
                    // LC              SAMPLE             SAMPLE             HC
                    95.45174575424718, 62.47955391280049, 77.93638152056002, 77.09337725442568,
                    89.97188793489511, 33.3073435903634, 99.33541818858255, 74.45653080903729,
                    66.75076984121507, 44.10727370863094, 52.94547610582584, 12.092348348947057));
                put("isPlateStat", isPlateStat);
                put("isWelltypeStat", isWelltypeStat);
            }}))
            .script("JavaStat::" + statCalculator.getName())
            .build();
        return javaStatExecutor.execute(input);
    }

    @Test
    public void testInvalidFormula() throws JsonProcessingException {
        var javaStatExecutor = new JavaStatExecutor(objectMapper, Collections.emptyList());
        var input = ScriptExecutionInputDTO.builder()
            .id("myId")
            .input(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
                put("lowWelltype", "LC");
                put("highWelltype", "HC");
                put("welltypes", List.of("LC", "SAMPLE", "SAMPLE", "HC", "LC", "SAMPLE", "SAMPLE", "HC", "LC", "SAMPLE", "SAMPLE", "HC"));
                put("featureValues", List.of(
                    // LC              SAMPLE             SAMPLE             HC
                    95.45174575424718, 62.47955391280049, 77.93638152056002, 77.09337725442568,
                    89.97188793489511, 33.3073435903634, 99.33541818858255, 74.45653080903729,
                    66.75076984121507, 44.10727370863094, 52.94547610582584, 12.092348348947057));
                put("isPlateStat", true);
                put("isWelltypeStat", true);
            }}))
            .script("AnInvalidFormula")
            .build();

        var output = javaStatExecutor.execute(input);
        Assertions.assertEquals("{\"plateValue\":null,\"welltypeValues\":{}}", output.getOutput());
        Assertions.assertEquals(ResponseStatusCode.BAD_REQUEST, output.getStatusCode());
        Assertions.assertEquals("Invalid formula: does not start with \"JavaStat::\"", output.getStatusMessage());
        Assertions.assertEquals(0, output.getExitCode());
        Assertions.assertEquals("myId", output.getInputId());
    }

    @Test
    public void testNoCalculatorFround() throws JsonProcessingException {
        var javaStatExecutor = new JavaStatExecutor(objectMapper, Collections.emptyList());
        var input = ScriptExecutionInputDTO.builder()
            .id("myId")
            .input(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
                put("lowWelltype", "LC");
                put("highWelltype", "HC");
                put("welltypes", List.of("LC", "SAMPLE", "SAMPLE", "HC", "LC", "SAMPLE", "SAMPLE", "HC", "LC", "SAMPLE", "SAMPLE", "HC"));
                put("featureValues", List.of(
                    // LC              SAMPLE             SAMPLE             HC
                    95.45174575424718, 62.47955391280049, 77.93638152056002, 77.09337725442568,
                    89.97188793489511, 33.3073435903634, 99.33541818858255, 74.45653080903729,
                    66.75076984121507, 44.10727370863094, 52.94547610582584, 12.092348348947057));
                put("isPlateStat", true);
                put("isWelltypeStat", true);
            }}))
            .script("JavaStat::mean")
            .build();

        var output = javaStatExecutor.execute(input);
        Assertions.assertEquals("{\"plateValue\":null,\"welltypeValues\":{}}", output.getOutput());
        Assertions.assertEquals(ResponseStatusCode.BAD_REQUEST, output.getStatusCode());
        Assertions.assertEquals("Invalid formula: no calculator found for this formula: \"mean\"", output.getStatusMessage());
        Assertions.assertEquals(0, output.getExitCode());
        Assertions.assertEquals("myId", output.getInputId());
    }

    @Test
    public void testInvalidJsonInInput() throws JsonProcessingException {
        var javaStatExecutor = new JavaStatExecutor(objectMapper, Collections.emptyList());
        var input = ScriptExecutionInputDTO.builder()
            .id("myId")
            .input("{\"invalid_json..")
            .script("JavaStat::mean")
            .build();

        var output = javaStatExecutor.execute(input);
        Assertions.assertEquals("{\"plateValue\":null,\"welltypeValues\":{}}", output.getOutput());
        Assertions.assertEquals(ResponseStatusCode.BAD_REQUEST, output.getStatusCode());
        Assertions.assertEquals("Invalid input format", output.getStatusMessage());
        Assertions.assertEquals(0, output.getExitCode());
        Assertions.assertEquals("myId", output.getInputId());
    }

    @Test
    public void testDuplicateExecutor() {
        var ex = Assertions.assertThrows(IllegalArgumentException.class, () -> new JavaStatExecutor(objectMapper, List.of(new MeanCalculator(), new MeanCalculator())));
        Assertions.assertEquals("Found duplicate StatCalculator: mean", ex.getMessage());
    }

}
