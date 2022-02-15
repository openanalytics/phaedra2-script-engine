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
package eu.openanalytics.phaedra.scriptengine.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.openanalytics.phaedra.scriptengine.dto.ResponseStatusCode;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionInputDTO;
import eu.openanalytics.phaedra.scriptengine.dto.ScriptExecutionOutputDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Random;

@Primary
@Component
@ConditionalOnProperty(name = "phaedra2.script-engine-worker.chaos.enabled", havingValue = "true")
public class ChaosExecutor implements IExecutor {

    private static final String PROP_CHAOS_EXCEPTION_PROBABILITY = "phaedra2.script-engine-worker.chaos.exception-probability";
    private static final String PROP_CHAOS_IGNORE_PROBABILITY = "phaedra2.script-engine-worker.chaos.ignore-probability";

    private final IExecutor executor;
    private final Random random = new Random();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int exceptionProbability;
    private final int ignoreProbability;

    public ChaosExecutor(IExecutor executor, Environment environment) {
        this.executor = executor;

        exceptionProbability = environment.getProperty(PROP_CHAOS_EXCEPTION_PROBABILITY, Integer.class, 0);
        ignoreProbability = environment.getProperty(PROP_CHAOS_IGNORE_PROBABILITY, Integer.class, 0);

        if (exceptionProbability < 0) {
            throw new IllegalArgumentException("Misconfiguration detected: the exception probability must be greater than 0");
        }

        if (ignoreProbability < 0) {
            throw new IllegalArgumentException("Misconfiguration detected: the ignore probability must be greater than 0");
        }

        if ((exceptionProbability + ignoreProbability) > 10) {
            throw new IllegalArgumentException("Misconfiguration detected: the sum of exception probability and ignore probability may not be greater than 10");
        }

        logger.warn("ChaosExecutor enabled, exception probability [{}/10], ignore probability [{}/10]", exceptionProbability, ignoreProbability);
    }


    @Override
    public ScriptExecutionOutputDTO execute(ScriptExecutionInputDTO scriptExecution) throws InterruptedException, JsonProcessingException {
        var number = random.ints(1, 0, 10).toArray()[0]; // generate 0 -> 9
        if (number < exceptionProbability) {
            // produce an internal error
            logger.warn("ChaosExecutor produced an error for ScriptExecution [{}]", scriptExecution.getId());
            return new ScriptExecutionOutputDTO(scriptExecution.getId(), "", ResponseStatusCode.WORKER_INTERNAL_ERROR, "[CHAOS] internal error created", 0);
        } else if (number < (exceptionProbability + ignoreProbability)) {
            // ignore the message. It was acked so the WatchDog should create a failure for it.
            logger.warn("ChaosExecutor ignored a ScriptExecution [{}]", scriptExecution.getId());
            return null;
        } else {
            // business as usual
            return executor.execute(scriptExecution);
        }
    }
}
