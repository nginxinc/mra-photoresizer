package com.nginx.image.health;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.io.Files;

import java.io.File;
import java.text.NumberFormat;

/**
 * Copyright (C) 2017 NGINX, Inc.
 */

public class DiskHealthCheck extends HealthCheck {
    public DiskHealthCheck() {}

    @Override
    protected Result check() throws Exception {
        File fileSystem = Files.createTempDir();;

        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxFreeSpace = fileSystem.getUsableSpace();
        long totalSpace = fileSystem.getTotalSpace();
        long freeSpace = fileSystem.getFreeSpace();

        sb.append("free disk space\": \"" + format.format(freeSpace / 1024) + "\",");
        sb.append("\"total disk space\": \"" + format.format(totalSpace / 1024) + "\",");
        sb.append("\"usable disk space\": \"" + format.format(maxFreeSpace / 1024) + "\",");
        sb.append("\"usable percentage of file system\": \"" + (((float) maxFreeSpace/totalSpace) * 100) + "%\"");
        if (((float) maxFreeSpace/totalSpace) < 0.05) {
            return Result.unhealthy(sb.toString());
        }
        return Result.healthy();
    }
}