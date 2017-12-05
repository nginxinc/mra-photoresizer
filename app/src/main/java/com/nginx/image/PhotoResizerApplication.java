package com.nginx.image;

import com.nginx.image.health.DiskHealthCheck;
import com.nginx.image.health.MemoryHealthCheck;
import com.nginx.image.resources.PhotoResizerResource;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 *  PhotoresizerApplication.java
 *  PhotoResizer
 *
 * Extends {@link io.dropwizard.Application} to dynamically build configuration
 *
 *  Copyright © 2017 NGINX Inc. All rights reserved.
 */
public class PhotoResizerApplication extends Application<PhotoResizerConfiguration> {

    /**
     * The main method that is called by the java executable
     *
     * Calls {@link #run(PhotoResizerConfiguration, Environment)}
     *
     * @param args String array of arguments from the command line
     * @throws Exception propagated from {@link io.dropwizard.Application#run(String...)}
     */
    public static void main(final String[] args) throws Exception {

        // call the overridden run method to start the application
        new PhotoResizerApplication().run(args);
    }

    /**
     * Overrides the {@link io.dropwizard.Application#getName()} method to
     * identify this application
     *
     * @return a string with the S3 URL used by the application
     */
    @Override
    public String getName() {
        return "A simple Photo Resizer Application";
    }

    /**
     * Overrides the {@link io.dropwizard.Application#initialize(Bootstrap)} method
     *
     * Enables the {@link io.dropwizard.configuration.EnvironmentVariableSubstitutor}
     * provider to allow for environment variables to be read in to the configuration
     *
     * @param bootstrap an instance of {@link io.dropwizard.setup.Bootstrap}
     */
    @Override
    public void initialize(final Bootstrap<PhotoResizerConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
    }

    /**
     * Overrides the {@link io.dropwizard.Application#run(Configuration, Environment)} method
     *
     * Instantiates and registers health checks for the environment
     *
     * @param configuration the PhotoResizerConfiguration object
     * @param environment the Environment object
     */
    @Override
    public void run(final PhotoResizerConfiguration configuration,
                    final Environment environment) {

        // add memory and disk health checks to environment
        environment.healthChecks().register("disk", new DiskHealthCheck());
        environment.healthChecks().register("memory", new MemoryHealthCheck());

        // register the PhotoResizerResource class 
        environment.jersey().register(new PhotoResizerResource(configuration.getS3Client().build(environment)));
    }
}
