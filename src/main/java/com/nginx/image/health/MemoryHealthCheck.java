package com.nginx.image.health;

import com.codahale.metrics.health.HealthCheck;

import java.text.NumberFormat;

/**
 * Created by cstetson on 10/9/15.
 */
public class MemoryHealthCheck extends HealthCheck
{
    public MemoryHealthCheck() {}

    @Override
    protected Result check() throws Exception {
        Runtime runtime = Runtime.getRuntime();

        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        sb.append("free memory: " + format.format(freeMemory / 1024) + "<br/>");
        sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "<br/>");
        sb.append("max memory: " + format.format(maxMemory / 1024) + "<br/>");
        sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "<br/>");
        if ((freeMemory/maxMemory) < .1) {
            return Result.unhealthy(sb.toString());
        }
        return Result.healthy();
    }
}