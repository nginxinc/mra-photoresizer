package com.nginx.image;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.EnvironmentVariableLookup;
import org.hibernate.validator.constraints.*;

/**
 * Created by cstetson on 10/9/15.
 * Copyright (C) 2015 Nginx, Inc.
 */

public class PhotoResizerConfiguration extends Configuration {

    static EnvironmentVariableLookup echoEnv = new EnvironmentVariableLookup();

    @NotEmpty
    private static String s3BucketName =  echoEnv.lookup("S3_BUCKET"); //this should be overridden by environment variables

    @NotEmpty
    private static String redisCacheUrl = echoEnv.lookup("REDIS_CACHE_URL"); //this should be overridden by environment variables

    @NotEmpty
    private static String redisCachePort = echoEnv.lookup("REDIS_CACHE_PORT"); //this should be overridden by environment variables

    @NotEmpty
    private final static String LARGE = "large";

    @NotEmpty
    private final static String MEDIUM = "medium";

    @NotEmpty
    private final static String SMALL = "small";

    @NotEmpty
    private final static Integer LARGE_SIZE = -1;//means stay the same

    @NotEmpty
    private final static Integer MEDIUM_SIZE = 640;

    @NotEmpty
    private final static Integer SMALL_SIZE = 128;

    @NotEmpty
    private final static Float COMPRESSION_QUALITY = 0.8F;

    @NotEmpty
    private static final ImmutableMap<String, Integer> sizesMap = ImmutableMap.of(
                LARGE, LARGE_SIZE,
                MEDIUM, MEDIUM_SIZE,
                SMALL, SMALL_SIZE
        );

    public static String getS3BucketName()
    {
        return s3BucketName;
    }

    public static String getRedisCacheUrl() { return redisCacheUrl; }//TODO:round robin the url

    public static Integer getRedisCachePort()
    {
        return new Integer(redisCachePort);
    }

    public static String getLARGE()
    {
        return LARGE;
    }

    public static String getMEDIUM()
    {
        return MEDIUM;
    }

    public static String getSMALL()
    {
        return SMALL;
    }

    public static Integer getLargeSize()
    {
        return LARGE_SIZE;
    }

    public static Integer getMediumSize()
    {
        return MEDIUM_SIZE;
    }

    public static Integer getSmallSize()
    {
        return SMALL_SIZE;
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
