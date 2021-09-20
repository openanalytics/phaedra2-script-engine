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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import eu.openanalytics.phaedra.scriptengine.executor.IExecutor;

import java.util.HashMap;

public class JavaStatExecutor implements IExecutor {

    private final ObjectMapper objectMapper;

    public JavaStatExecutor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ScriptExecutionOutputDTO execute(ScriptExecutionInputDTO scriptExecutionInput) throws InterruptedException, JsonProcessingException {
        System.out.println(scriptExecutionInput);

        var output = new Output();
        output.setPlateValue(1.42f);
        output.addWelltypeValue("LC", 2.42f);
        output.addWelltypeValue("HC", 3.42f);

        var res = ScriptExecutionOutputDTO.builder()
            .inputId(scriptExecutionInput.getId())
            .statusCode(ResponseStatusCode.SUCCESS)
            .statusMessage("Ok")
            .exitCode(0)
            .output(objectMapper.writeValueAsString(output));

        return res.build();
    }

    public static class Output {

        private Float plateValue = null;

        private final HashMap<String, Float> welltypeValues = new HashMap<>();

        public void addWelltypeValue(String welltype, Float value) {
            welltypeValues.put(welltype, value);
        }

        public void setPlateValue(Float value) {
            plateValue = value;
        }

        public Float getPlateValue() {
            return plateValue;
        }

        public HashMap<String, Float> getWelltypeValues() {
            return welltypeValues;
        }

    }

}
