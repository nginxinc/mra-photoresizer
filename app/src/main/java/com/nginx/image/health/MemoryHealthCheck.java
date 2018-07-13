package com.nginx.image.health;

import com.codahale.metrics.health.HealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.text.NumberFormat;

/**
 * MemoryHealthCheck.java
 * PhotoResizer
 *
 * Extension of the {@link com.codahale.metrics.health.HealthCheck} abstract class
 * ensures that there is enough memory to run the resizer service
 *
 * Copyright © 2018 NGINX Inc. All rights reserved.
 */
public class MemoryHealthCheck extends HealthCheck {

    // Message to show if there is not enough disk space
    private static final String ERROR_MESSAGE_STRING = "free memory: {0}, " +
            "allocated memory: {1}, max memory: {2}, percent of memory used: {3, number, percent}%";

    // The logger for this instance of the service
    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryHealthCheck.class);

    private final Runtime runtime;
    private double memoryThreshold;

    /**
     * Default no-arg constructor
     *
     * Instantiates the runtime variable by calling {@link Runtime#getRuntime()}
     *
     * @param memoryThresholdParam The double value used to calculate when memory is being used up, e.g. 0.8 = 80% of available memory

     */
    public MemoryHealthCheck(double memoryThresholdParam)
    {
        runtime = Runtime.getRuntime();
        memoryThreshold = memoryThresholdParam;
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
        if ((1 - ((float)freeMemory/allocatedMemory)) > memoryThreshold) {
            String errorMessage = memoryUsed(allocatedMemory, freeMemory);
            LOGGER.error("*****UNHEALTHY*******: " + errorMessage);
            runtime.gc();
            LOGGER.error("*****GC Called*******: " + errorMessage);
            return Result.unhealthy(errorMessage);
        }
        LOGGER.debug(memoryUsed(allocatedMemory, freeMemory));
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