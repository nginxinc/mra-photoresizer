package com.nginx.image;

import com.nginx.image.health.DiskHealthCheck;
import com.nginx.image.health.MemoryHealthCheck;
import com.nginx.image.resources.PhotoResizerResource;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class PhotoResizerApplication extends Application<PhotoResizerConfiguration> {

    public static void main(final String[] args) throws Exception {
        new PhotoResizerApplication().run(args);
    }

    @Override
    public String getName() {
        return "A simple Photo Resizer Application";
    }

    @Override
    public void initialize(final Bootstrap<PhotoResizerConfiguration> bootstrap)
    {
        
            // Enable variable substitution with environment variables
            bootstrap.setConfigurationSourceProvider(
                    new SubstitutingSourceProvider(
                            bootstrap.getConfigurationSourceProvider(),
                            new EnvironmentVariableSubstitutor(false)
                    )
            );
    }

    @Override
    public void run(final PhotoResizerConfiguration configuration,
                    final Environment environment)
    {
        final PhotoResizerResource resource = new PhotoResizerResource();

        final DiskHealthCheck diskHealthCheck = new DiskHealthCheck();
        environment.healthChecks().register("disk", diskHealthCheck);

        final MemoryHealthCheck memoryHealthCheck = new MemoryHealthCheck();
        environment.healthChecks().register("memory", memoryHealthCheck);

        environment.jersey().register(resource);
    }

}
