package com.nginx.image.health;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.io.Files;

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
 * Copyright © 2017 NGINX Inc. All rights reserved.
 */
public class DiskHealthCheck extends HealthCheck {

    // Message to show if there is not enough disk space
    private static final String ERROR_MESSAGE_STRING = "free disk space: {0}, " +
            "total disk space: {1}, usable disk space: {2}, usable percentage of file system: {3, number, percent}%";

    /**
     * Default no-arg constructor
     */
    public DiskHealthCheck() {}

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
        if (((float) maxFreeSpace/totalSpace) < 0.05) {
            // if there is not enough disk space, get the free space to
            // format the message and return Result.unhealthy
            long freeSpace = fileSystem.getFreeSpace();
            return Result.unhealthy(MessageFormat.format(ERROR_MESSAGE_STRING,
                    format.format(freeSpace / 1024),
                    format.format(totalSpace / 1024),
                    format.format(maxFreeSpace / 1024),
                    ((float) maxFreeSpace / totalSpace) * 100)
            );
        }

        // disk space is greater then 0.05% free
        return Result.healthy();
    }
}