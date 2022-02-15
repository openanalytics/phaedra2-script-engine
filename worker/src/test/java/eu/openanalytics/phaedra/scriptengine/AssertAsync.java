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
package eu.openanalytics.phaedra.scriptengine;

import org.junit.jupiter.api.Assertions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AssertAsync {

    private final Future<Boolean> executed;

    public AssertAsync(Runnable executable) {
        ExecutorService es = Executors.newSingleThreadExecutor();
        executed = es.submit(() -> {
            executable.run();
            return true;
        });
    }

    public void assertCalled(long timeout) throws ExecutionException, InterruptedException, TimeoutException {
        Boolean res = executed.get(timeout, TimeUnit.MILLISECONDS);
        Assertions.assertNotNull(res);
        Assertions.assertTrue(res);
    }

}
