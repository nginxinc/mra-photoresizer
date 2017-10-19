package com.nginx.image;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.EnvironmentVariableLookup;
import org.hibernate.validator.constraints.*;

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
     * Constant for the Large image size
     *
     * Cannot be null or whitespace as specified by
     * {@link org.hibernate.validator.constraints.NotEmpty}
     */
    @NotEmpty
    public final static String LARGE = "large";

    /**
     * Constant for the Medium image size
     *
     * Cannot be null or whitespace as specified by
     * {@link org.hibernate.validator.constraints.NotEmpty}
     */
    @NotEmpty
    public final static String MEDIUM = "medium";

    /**
     * Constant for the Thumbnail image size
     *
     * Cannot be null or whitespace as specified by
     * {@link org.hibernate.validator.constraints.NotEmpty}
     */
    @NotEmpty
    public final static String THUMB = "thumb";

    /**
     * Constant for the Large image size in width
     *
     * In this implementation the large size is always the same size as the
     * original, so the value is a sentinel set to -1
     *
     * Cannot be null as specified by
     * {@link org.hibernate.validator.constraints.NotEmpty}
     */
    @NotEmpty
    private final static Integer LARGE_SIZE = -1; // Means stay the same

    /**
     * Constant for the Medium image size in pixel width
     *
     * The medium size of an image is 640 pixels
     *
     * Cannot be null as specified by
     * {@link org.hibernate.validator.constraints.NotEmpty}
     */
    @NotEmpty
    private final static Integer MEDIUM_SIZE = 640;

    /**
     * Constant for the Small image size in pixel width
     *
     * The medium size of an image is 128 pixels
     *
     * Cannot be null as specified by
     * {@link org.hibernate.validator.constraints.NotEmpty}
     */
    @NotEmpty
    private final static Integer THUMB_SIZE = 128;

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
     * Immutable map to hold the constants specified for each of the image size
     * definitions.
     *
     * TODO: Replace this with enums in a future version
     *
     * Cannot be null as specified by
     * {@link org.hibernate.validator.constraints.NotEmpty}
     */
    @NotEmpty
    public static final ImmutableMap<String, Integer> SIZES_MAP = ImmutableMap.of(
            LARGE, LARGE_SIZE,
            MEDIUM, MEDIUM_SIZE,
            THUMB, THUMB_SIZE
    );

    /**
     * s3BucketName private variable set from the S3_BUCKET environment variable
     *
     * Specifies the name of the S3 bucket to use when building image URLs
     *
     * Cannot be null or whitespace as specified by
     * {@link org.hibernate.validator.constraints.NotEmpty}
     */
    @NotEmpty
    private static final String s3BucketName = echoEnv.lookup("S3_BUCKET");

    /**
     * accessKey private variable set from the AWS_ACCESS_KEY_ID environment variable
     *
     * Specifies the name of the AWS access key credential to use when uploading
     * images
     *
     * Cannot be null or whitespace as specified by
     * {@link org.hibernate.validator.constraints.NotEmpty}
     */
    @NotEmpty
    private static final String accessKey = echoEnv.lookup("AWS_ACCESS_KEY_ID");

    /**
     * secretKey private variable set from the AWS_SECRET_ACCESS_KEY_ID environment variable
     *
     * Specifies the name of the AWS secret access key credential to use when uploading
     * images
     *
     * Cannot be null or whitespace as specified by
     * {@link org.hibernate.validator.constraints.NotEmpty}
     */

    @NotEmpty
    private static final String secretKey = echoEnv.lookup("AWS_SECRET_ACCESS_KEY");

    /**
     * s3URL private variable set from the S3_URL environment variable
     *
     * Specifies the name of the AWS S3 location to use when uploading and downloading
     * images
     *
     * Cannot be null or whitespace as specified by
     * {@link org.hibernate.validator.constraints.NotEmpty}
     */
    @NotEmpty
    private static final String s3URL = echoEnv.lookup("S3_URL");

    /**
     * Getter for the s3BucketName property
     * @return a String of the s3BucketName variable
     */
    public static String getS3BucketName() {
        return s3BucketName;
    }

    /**
     * Getter for the accessKey property
     * @return a String of the accessKey variable
     */
    public static String getAccessKey() {
        return accessKey;
    }

    /**
     * Getter for the secretKey property
     * @return a String of the secretKey variable
     */
    public static String getSecretKey() {
        return secretKey;
    }

    /**
     * Getter for the compressionQuality property
     * @return a String of the compressionQuality variable
     */
    public static Float getCompressionQuality() {
        return COMPRESSION_QUALITY;
    }


    /**
     * Getter for the s3URL property
     * @return a String of the s3URL variable
     */
    public static String getS3URL() {
        return s3URL;
    }
}
