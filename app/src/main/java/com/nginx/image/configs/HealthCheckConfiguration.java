package com.nginx.image.configs;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.EnvironmentVariableLookup;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 *  ResizerConfiguration.java
 *  PhotoResizer
 *
 *  Copyright © 2018 NGINX Inc. All rights reserved.
 *
 *  This class extends {@link Configuration}
 */

public class HealthCheckConfiguration extends Configuration {
    private static final EnvironmentVariableLookup echoEnv =
            new EnvironmentVariableLookup();

    /**
     * CPUHealthCheck
     */
    @Valid
    @NotNull
    private double cpuThreshold;

    /**
     * MemoryHealthCheck
     */
    @Valid
    @NotNull
    private double memoryThreshold;

    /**
     * DiskHealthCheck
     */
    @Valid
    @NotNull
    private double diskThreshold;

    public static EnvironmentVariableLookup getEchoEnv()
    {
        return echoEnv;
    }

   /**
     * Getter for cpuThreshold
     * @return double
     */
    @JsonProperty
    public double getCpuThreshold()
    {
        return cpuThreshold;
    }

    /**
     * Setter for cpuThreshold
     * @param cpuThreshold The double value used to calculate when the CPU is being used up, e.g. 0.8 = 80% of a CPU
     */
    @JsonProperty
    public void setCpuThreshold(double cpuThreshold)
    {
        this.cpuThreshold = cpuThreshold;
    }

    /**
     * Getter for memoryThreshold
     * @return double
     */
    @JsonProperty
    public double getMemoryThreshold()
    {
        return memoryThreshold;
    }

    /**
     * Setter for memoryThreshold
     * @param memoryThreshold The double value used to calculate when memory is being used up, e.g. 0.8 = 80% of a memory
     */
    @JsonProperty
    public void setMemoryThreshold(double memoryThreshold)
    {
        this.memoryThreshold = memoryThreshold;
    }

    /**
     * Setter for diskThreshold
     * @return double
     */
    @JsonProperty
    public double getDiskThreshold() { return diskThreshold; }

    /**
     * Setter for diskThreshold
     * @param diskThreshold The double value used to calculate when the disk is being used up, e.g. 0.05 = 5% of disk is left
     */
    @JsonProperty
    public void setDiskThreshold(double diskThreshold) { this.diskThreshold = diskThreshold; }

}
