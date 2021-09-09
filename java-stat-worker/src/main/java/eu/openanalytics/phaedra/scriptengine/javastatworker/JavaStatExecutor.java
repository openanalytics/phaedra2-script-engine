package eu.openanalytics.phaedra.scriptengine.javastatworker;
/**
 * ContainerProxy
 *
 * Copyright (C) 2016-2021 Open Analytics
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutor;
import org.apache.commons.math.stat.StatUtils;

import java.util.List;

public class JavaStatExecutor implements IExecutor {

    private final ObjectMapper objectMapper;

    public JavaStatExecutor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ScriptExecutionOutputDTO execute(ScriptExecutionInputDTO scriptExecutionInput) throws InterruptedException, JsonProcessingException {
        var input = objectMapper.readValue(scriptExecutionInput.getInput(), Input.class);
        var formula = scriptExecutionInput.getScript();

        var res = ScriptExecutionOutputDTO.builder()
            .inputId(scriptExecutionInput.getId())
            .statusCode(ResponseStatusCode.SUCCESS)
            .statusMessage("Ok")
            .exitCode(0);

        switch (formula) {
            case "MAX" -> res.output(objectMapper.writeValueAsString(new Output(StatUtils.max(input.getValues()))));
            case "MIN" -> res.output(objectMapper.writeValueAsString(new Output(StatUtils.min(input.getValues()))));
            case "SUM" -> res.output(objectMapper.writeValueAsString(new Output(StatUtils.sum(input.getValues()))));
        }

        return res.build();
    }

    public static class Output {
        private double output;

        public Output(double output) {
            this.output = output;
        }

        public double getOutput() {
            return output;
        }

        public void setOutput(double output) {
            this.output = output;
        }
    }

    public static class Input {

        private double[] values;
        private List<String> wellTypes;

        public double[] getValues() {
            return values;
        }

        public void setValues(double[] values) {
            this.values = values;
        }

        public List<String> getWellTypes() {
            return wellTypes;
        }

        public void setWellTypes(List<String> wellTypes) {
            this.wellTypes = wellTypes;
        }

        @JsonCreator
        public Input(@JsonProperty(value = "values") double[] values, @JsonProperty(value = "wellTypes") List<String> wellTypes) {
            this.values = values;
            this.wellTypes = wellTypes;
        }
    }


}
