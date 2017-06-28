package com.nginx.image.health;

import com.codahale.metrics.health.HealthCheck;

import java.text.NumberFormat;

/**
 //  MemoryHealthCheck.java
 //  PhotoResizer
 //
 //  Copyright © 2017 NGINX Inc. All rights reserved.
 */

public class MemoryHealthCheck extends HealthCheck {
    public MemoryHealthCheck() {}

    @Override
    protected Result check() throws Exception {
        Runtime runtime = Runtime.getRuntime();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        StringBuilder sb = memoryUsed();
        if ((1 - ((float)freeMemory/allocatedMemory)) > 0.8) {
            System.out.println("*****UNHEALTHY*******: " + sb.toString());
            runtime.gc();
            System.out.println("*****GC Called*******: " + sb.toString());
            return Result.unhealthy(sb.toString());
        }
        return Result.healthy();
    }

    private StringBuilder memoryUsed() {
        Runtime runtime = Runtime.getRuntime();

        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        sb.append("free memory\": \"").append(format.format(freeMemory / 1024)).append("\",");
        sb.append("\"allocated memory\": \"").append(format.format(allocatedMemory / 1024)).append("\",");
        sb.append("\"max memory\": \"").append(format.format(maxMemory / 1024)).append("\n");
        sb.append("\"percentage of memory used:\": \"").append((1 - ((float) freeMemory / allocatedMemory)) * 100).append("%\",");
        return sb;
    }
}