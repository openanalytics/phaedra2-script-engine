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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaStatExecutor implements IExecutor {

    private final ObjectMapper objectMapper;

    private final Map<String, StatCalculator> statCalculators = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public JavaStatExecutor(ObjectMapper objectMapper, List<StatCalculator> statCalculators) {
        this.objectMapper = objectMapper;
        for (var statCalculator : statCalculators) {
            if (this.statCalculators.containsKey(statCalculator.getName())) {
                throw new IllegalArgumentException("TODO"); // TODO
            }
            logger.info(String.format("Mapping formula name \"%s\" to class %s", statCalculator.getName(), statCalculator.getClass().getSimpleName()));
            this.statCalculators.put(statCalculator.getName(), statCalculator);
        }
    }

    @Override
    public ScriptExecutionOutputDTO execute(ScriptExecutionInputDTO scriptExecutionInput) {
        try {
            var input = objectMapper.readValue(scriptExecutionInput.getInput(), CalculationInput.class);

            var formula = scriptExecutionInput.getScript();

            if (!formula.startsWith("JavaStat::")) {
                return error(scriptExecutionInput, ResponseStatusCode.BAD_REQUEST, "Invalid formula: does not start with \"JavaStat::\"");
            }

            var statName = formula.replace("JavaStat::", "");
            var calculator = statCalculators.get(statName);

            if (calculator == null) {
                return error(scriptExecutionInput, ResponseStatusCode.SCRIPT_ERROR, "Invalid formula: no calculator found for this formula: \"%s\"");
            }

            logger.info(String.format("Executing ScriptExecutionInput: [id: %s, calculator: %s] ", scriptExecutionInput.getId(), calculator.getName()));
            var outputBuilder = CalculationOutput.builder();

            try {
                if (input.isPlateStat()) {
                    outputBuilder.plateValue(calculator.calculateForPlate(input));
                }
                if (input.isWelltypeStat()) {
                    for (var group : input.getValuesByWelltype().entrySet()) {
                        var value = calculator.calculateForWelltype(input, group.getKey(), group.getValue());
                        outputBuilder.addWelltypeValue(group.getKey(), value);
                    }
                }
            } catch (Throwable ex) {
                return error(scriptExecutionInput, ResponseStatusCode.SCRIPT_ERROR, "Exception during execution of stat");
            }

            var res = ScriptExecutionOutputDTO.builder()
                .inputId(scriptExecutionInput.getId())
                .statusCode(ResponseStatusCode.SUCCESS)
                .statusMessage("Ok")
                .exitCode(0)
                .output(objectMapper.writeValueAsString(outputBuilder.build()));

            logger.info(String.format("Executed ScriptExecutionInput: [id: %s, calculator: %s, statusCode: SUCCESS] ", scriptExecutionInput.getId(), calculator.getName()));
            return res.build();
        } catch (JsonProcessingException ex) {
            return error(scriptExecutionInput, ResponseStatusCode.BAD_REQUEST, "Invalid input format");
        }
    }

    private ScriptExecutionOutputDTO error(ScriptExecutionInputDTO scriptExecutionInput, ResponseStatusCode statusCode, String statusMessage) {
        var res = ScriptExecutionOutputDTO.builder()
            .inputId(scriptExecutionInput.getId())
            .statusCode(statusCode)
            .statusMessage(statusMessage)
            .exitCode(0)
            .build();

        logger.info(String.format("Executed ScriptExecutionInput: [id: %s, calculator: %s, statusCode: %s, statusMessage] ", scriptExecutionInput.getId(), statusCode, statusMessage));
        return res;

    }


}
