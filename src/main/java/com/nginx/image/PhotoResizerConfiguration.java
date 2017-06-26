package com.nginx.image;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.EnvironmentVariableLookup;
import org.hibernate.validator.constraints.*;

/**
 * Copyright (C) 2017 NGINX, Inc.
 */

public class PhotoResizerConfiguration extends Configuration {
    private static EnvironmentVariableLookup echoEnv = new EnvironmentVariableLookup();

    @NotEmpty
    private static String s3BucketName =  echoEnv.lookup("S3_BUCKET");

    @NotEmpty
    private final static String LARGE = "large";

    @NotEmpty
    private final static String MEDIUM = "medium";

    @NotEmpty
    private final static String THUMB = "thumb";

    @NotEmpty
    private final static Integer LARGE_SIZE = -1; // Means stay the same

    @NotEmpty
    private final static Integer MEDIUM_SIZE = 640;

    @NotEmpty
    private final static Integer THUMB_SIZE = 128;

    @NotEmpty
    private final static Float COMPRESSION_QUALITY = 0.8F;

    @NotEmpty
    private static final ImmutableMap<String, Integer> sizesMap = ImmutableMap.of(
            LARGE, LARGE_SIZE,
            MEDIUM, MEDIUM_SIZE,
            THUMB, THUMB_SIZE
    );

    public static String getS3BucketName()
    {
        return s3BucketName;
    }

    public static String getLARGE()
    {
        return LARGE;
    }

    public static String getMEDIUM()
    {
        return MEDIUM;
    }

    public static String getTHUMB()
    {
        return THUMB;
    }

    public static Integer getLargeSize()
    {
        return LARGE_SIZE;
    }

    public static Integer getMediumSize()
    {
        return MEDIUM_SIZE;
    }

    public static Integer getThumbSize()
    {
        return THUMB_SIZE;
    }

    public static ImmutableMap<String, Integer> getSizesMap()
    {
        return sizesMap;
    }

    public static Float getCompressionQuality()
    {
        return COMPRESSION_QUALITY;
    }
}
