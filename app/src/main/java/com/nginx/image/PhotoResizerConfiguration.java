package com.nginx.image;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nginx.image.net.S3ClientFactory;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.EnvironmentVariableLookup;
import org.hibernate.validator.constraints.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 *  PhotoResizerConfiguration.java
 *  PhotoResizer
 *
 *  Copyright © 2017 NGINX Inc. All rights reserved.
 *
 *  This class extends {@link io.dropwizard.Configuration}
 */

public class PhotoResizerConfiguration extends Configuration {
    private static final EnvironmentVariableLookup echoEnv =
            new EnvironmentVariableLookup();

    /**
     * Constant for the compression quality to use when resizing the images
     *
     * In this implementation, the value is set to .8, or 80% of the quality
     * of the original image
     *
     * Cannot be null as specified by
     * {@link org.hibernate.validator.constraints.NotEmpty}
     */
    @NotEmpty
    private final static Float COMPRESSION_QUALITY = 0.8F;


    /**
     * S3Client
     */
    @Valid
    @NotNull
    private S3ClientFactory s3Client = new S3ClientFactory();

    /**
     * Getter for s3Client
     *
     * @return com.nginx.image.net.S3ClientFactory
     */
    @JsonProperty
    public S3ClientFactory getS3Client() {
        return s3Client;
    }

    /**
     * Setter for s3Client
     */
    @JsonProperty
    public void setS3Client(S3ClientFactory s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Getter for the compressionQuality property
     * @return a String of the compressionQuality variable
     */
    public static Float getCompressionQuality() {
        return COMPRESSION_QUALITY;
    }
}
