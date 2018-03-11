package com.nginx.image.health;

import com.codahale.metrics.health.HealthCheck;

import java.text.MessageFormat;
import java.text.NumberFormat;

/**
 * MemoryHealthCheck.java
 * PhotoResizer
 *
 * Extension of the {@link com.codahale.metrics.health.HealthCheck} abstract class
 * ensures that there is enough memory to run the resizer service
 *
 * Copyright © 2017 NGINX Inc. All rights reserved.
 */
public class MemoryHealthCheck extends HealthCheck {

    // Message to show if there is not enough disk space
    private static final String ERROR_MESSAGE_STRING = "free memory: {0}, " +
            "allocated memory: {1}, max memory: {2}, percent of memory used: {3, number, percent}%";

    private final Runtime runtime;

    /**
     * Default no-arg constructor
     *
     * Instantiates the runtime variable by calling {@link Runtime#getRuntime()}
     */
    public MemoryHealthCheck() {
        runtime = Runtime.getRuntime();
    }

    /**
     * Implementation of the {@link HealthCheck#check()} method which evaluates the
     * current memory in use
     *
     * @return {@link Result#healthy()} if less than 80% of memory is used,
     *          otherwise return {@link Result#unhealthy(String)}
     *
     * @throws Exception inherited from parent method signature
     */
    @Override
    protected Result check() throws Exception {

        // get the total memory and free memory to use for the health check
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        // evaluate whether there is 80% or more memory free
        if ((1 - ((float)freeMemory/allocatedMemory)) > 0.8) {
            String errorMessage = memoryUsed(allocatedMemory, freeMemory);
            System.out.println("*****UNHEALTHY*******: " + errorMessage);
            runtime.gc();
            System.out.println("*****GC Called*******: " + errorMessage);
            return Result.unhealthy(errorMessage);
        }
        return Result.healthy();
    }

    /**
     * Private helper method used to display current memory information
     *
     * @return a String by populating the ERROR_MESSAGE_STRING with current memory values
     */
    private String memoryUsed(long allocatedMemory, long freeMemory) {

        // Instantiate a NumberFormat to parse the memory values String objects
        NumberFormat format = NumberFormat.getInstance();

        // get the max memory
        long maxMemory = runtime.maxMemory();

        // return the ERROR_MESSAGE_STRING formatted with the current memory info
        return MessageFormat.format(ERROR_MESSAGE_STRING,
                format.format(freeMemory / 1024),
                format.format(allocatedMemory / 1024),
                format.format(maxMemory / 1024),
                (1 - ((float) freeMemory / allocatedMemory)) * 100
        );
    }
}