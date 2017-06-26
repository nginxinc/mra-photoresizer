package com.nginx.image.health;

import com.codahale.metrics.health.HealthCheck;

import java.text.NumberFormat;

/**
 * Copyright (C) 2017 NGINX, Inc.
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
            System.out.println("*****GC Called*******: " + sb.toString());;
            return Result.unhealthy(sb.toString());
        }
        return Result.healthy();
    }

    protected StringBuilder memoryUsed() {
        Runtime runtime = Runtime.getRuntime();

        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        sb.append("free memory\": \"" + format.format(freeMemory / 1024) + "\",");
        sb.append("\"allocated memory\": \"" + format.format(allocatedMemory / 1024) + "\",");
        sb.append("\"max memory\": \"" + format.format(maxMemory / 1024) + "\n");
        sb.append("\"percentage of memory used:\": \"" + (1 - ((float) freeMemory/allocatedMemory)) * 100 + "%\",");
        return sb;
    }
}