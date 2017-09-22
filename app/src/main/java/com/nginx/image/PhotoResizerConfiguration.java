package com.nginx.image;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.EnvironmentVariableLookup;
import org.hibernate.validator.constraints.*;

/**
 //  PhotoResizerConfiguration.java
 //  PhotoResizer
 //
 //  Copyright © 2017 NGINX Inc. All rights reserved.
 */

public class PhotoResizerConfiguration extends Configuration {
    private static final EnvironmentVariableLookup echoEnv = new EnvironmentVariableLookup();

    @NotEmpty
    private static final String s3BucketName = echoEnv.lookup("S3_BUCKET");

    @NotEmpty
    private static final String accessKey = echoEnv.lookup("AWS_ACCESS_KEY_ID");

    @NotEmpty
    private static final String secretKey = echoEnv.lookup("AWS_SECRET_ACCESS_KEY");

    @NotEmpty
    private static final String fakeS3URL = echoEnv.lookup("FAKE_S3_URL");

    //@NotEmpty
    //private static final String s3Endpoint = "http://fake-s3.mra.nginxps.com";//echoEnv.lookup("S3_ENDPOINT");

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

    public static String getS3BucketName() {
        return s3BucketName;
    }

    public static String getAccessKey() {
        return accessKey;
    }

    //public static String getS3Endpoint() {
    //    return s3Endpoint;
    //}

    public static String getSecretKey() {
        return secretKey;
    }

    public static String getLARGE() {
        return LARGE;
    }

    public static String getMEDIUM() {
        return MEDIUM;
    }

    public static String getTHUMB() {
        return THUMB;
    }

    public static ImmutableMap<String, Integer> getSizesMap() {
        return sizesMap;
    }

    public static Float getCompressionQuality() {
        return COMPRESSION_QUALITY;
    }

    /**
     * Getter for fakeS3URL
     *
     * @return String
     */
    public static String getFakeS3URL() {
        return fakeS3URL;
    }
}
