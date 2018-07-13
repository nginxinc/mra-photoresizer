package com.nginx.image.health;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.MessageFormat;
import java.text.NumberFormat;

/**
 * DiskHealthCheck.java
 * PhotoResizer
 *
 * Extension of {@link com.codahale.metrics.health.HealthCheck} which is used
 * to check the amount of disk space available to the resizer service
 *
 *
 * Copyright © 2018 NGINX Inc. All rights reserved.
 */
public class DiskHealthCheck extends HealthCheck {

    // Message to show if there is not enough disk space
    private static final String ERROR_MESSAGE_STRING = "free disk space: {0}, " +
            "total disk space: {1}, usable disk space: {2}, usable percentage of file system: {3, number, percent}%";
    private double diskThreshold;
    private static final Logger LOGGER = LoggerFactory.getLogger(DiskHealthCheck.class);

    /**
     * Default constructor
     *
     * @param diskThresholdParam The double value used to calculate when the disk is being used up, e.g. 0.05 = 5% of a disk left
     */
    public DiskHealthCheck(double diskThresholdParam)
    {
        diskThreshold = diskThresholdParam;
    }

    /**
     * Implements the {@link HealthCheck#check()} method provides metrics regarding the available
     * disk space.
     *
     * @return {@link Result#healthy()} if there is at least 0.05% of disk space available
 *              otherwise return {@link Result#unhealthy(String)}
     *
     * @throws Exception inherited from method signature
     */
    @Override
    protected Result check() throws Exception {

        // Create a directory in the temporary file system
        File fileSystem = Files.createTempDir();

        // Used for formatting the message
        NumberFormat format = NumberFormat.getInstance();

        // values to use in disk space calculation
        long maxFreeSpace = fileSystem.getUsableSpace();
        long totalSpace = fileSystem.getTotalSpace();

        // require at least 0.05% disk space
        if (((float) maxFreeSpace/totalSpace) < diskThreshold) {
            // if there is not enough disk space, get the free space to
            // format the message and return Result.unhealthy
            long freeSpace = fileSystem.getFreeSpace();
            String errorMessage = MessageFormat.format(ERROR_MESSAGE_STRING,
                    format.format(freeSpace / 1024),
                    format.format(totalSpace / 1024),
                    format.format(maxFreeSpace / 1024),
                    ((float) maxFreeSpace / totalSpace) * 100);
            LOGGER.error(errorMessage);
            return Result.unhealthy(errorMessage);
        }

        // disk space is greater then 0.05% free
        return Result.healthy();
    }
}