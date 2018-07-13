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

public class PhotoResizerConfiguration extends Configuration {
    private static final EnvironmentVariableLookup echoEnv =
            new EnvironmentVariableLookup();

    @Valid
    @NotNull
    @JsonProperty
    private HealthCheckConfiguration healthCheckConfiguration = new HealthCheckConfiguration();

    public HealthCheckConfiguration getHealthCheckConfiguration() {
        return healthCheckConfiguration;
    }

    @Valid
    @NotNull
    @JsonProperty
    private ImageSizeConfiguration imageSizeConfiguration = new ImageSizeConfiguration();

    public ImageSizeConfiguration getImageSizeConfiguration() {
        return imageSizeConfiguration;
    }

    @Valid
    @NotNull
    @JsonProperty
    private ResizerConfiguration resizerConfiguration = new ResizerConfiguration();

    public ResizerConfiguration getResizerConfiguration() {
        return resizerConfiguration;
    }


}
