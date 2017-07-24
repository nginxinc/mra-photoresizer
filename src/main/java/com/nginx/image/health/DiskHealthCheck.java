package com.nginx.image.health;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.io.Files;

import java.io.File;
import java.text.NumberFormat;

/**
 //  DiskHealthCheck.java
 //  PhotoResizer
 //
 //  Copyright © 2017 NGINX Inc. All rights reserved.
 */

public class DiskHealthCheck extends HealthCheck {
    public DiskHealthCheck() {}

    @Override
    protected Result check() throws Exception {
        File fileSystem = Files.createTempDir();

        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxFreeSpace = fileSystem.getUsableSpace();
        long totalSpace = fileSystem.getTotalSpace();
        long freeSpace = fileSystem.getFreeSpace();

        sb.append("free disk space\": \"").append(format.format(freeSpace / 1024)).append("\",");
        sb.append("\"total disk space\": \"").append(format.format(totalSpace / 1024)).append("\",");
        sb.append("\"usable disk space\": \"").append(format.format(maxFreeSpace / 1024)).append("\",");
        sb.append("\"usable percentage of file system\": \"").append(((float) maxFreeSpace / totalSpace) * 100).append("%\"");
        if (((float) maxFreeSpace/totalSpace) < 0.05) {
            return Result.unhealthy(sb.toString());
        }
        return Result.healthy();
    }
}