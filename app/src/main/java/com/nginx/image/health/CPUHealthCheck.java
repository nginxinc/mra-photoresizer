package com.nginx.image.health;

import com.codahale.metrics.health.HealthCheck;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import org.slf4j.LoggerFactory;

/**
 * CPUHealthCheck.java
 * PhotoResizer
 *
 * Extension of the {@link com.codahale.metrics.health.HealthCheck} abstract class
 * ensures that there is enough memory to run the resizer service
 *
 * Copyright © 2018 NGINX Inc. All rights reserved.
 */
public class CPUHealthCheck extends HealthCheck {

    // Message to show if the system is using up too much CPU
    private static final String ERROR_MESSAGE_STRING = "cpu usage: {0}";
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CPUHealthCheck.class);

    private double cpuThreshold;
    private OperatingSystemMXBean cpuBean;

    /**
     * Default constructor
     *
     * Instantiates the runtime variable by calling {@link Runtime#getRuntime()}
     *
     * @param cpuThresholdParam The double value used to calculate when the CPU is being used up, e.g. 0.8 = 80% of a CPU
     */
    public CPUHealthCheck(double cpuThresholdParam)
    {
        cpuBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        cpuThreshold = cpuThresholdParam;
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
        double cpuUsage = cpuBean.getProcessCpuLoad();

        // evaluate whether there is 80% or more CPU used
        String cpuPercent = String.valueOf((int) (cpuUsage * 100.0))  + "%";
        LOGGER.debug(cpuPercent + " at time of healthcheck");

        if (cpuUsage > cpuThreshold) {
            String errorMessage = cpuUsed(cpuPercent);
            LOGGER.error("*****UNHEALTHY*******: " + errorMessage);
            return Result.unhealthy(errorMessage);
        }
        return Result.healthy();
    }

    /**
     * Private helper method used to display current memory information
     *
     * @return a String by populating the ERROR_MESSAGE_STRING with current memory values
     */
    private String cpuUsed(String cpuUsage) {

        // return the ERROR_MESSAGE_STRING formatted with the current CPU info
        return MessageFormat.format(ERROR_MESSAGE_STRING,cpuUsage);
    }
}