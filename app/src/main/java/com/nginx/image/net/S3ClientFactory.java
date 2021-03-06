package com.nginx.image.net;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Created 12/2/17 16:06 with IntelliJ IDEA.
 * User: charlespretzer
 */
public class S3ClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientFactory.class);

    @NotEmpty
    private String existingBucketName;

    @NotEmpty
    private String s3Url;

    @NotEmpty
    private String accessKey;

    @NotEmpty
    private String secretKey;

    @Min(0)
    @Max(65535)
    private int s3Retries = 3;

    public S3Client build(Environment environment) {
        S3Client s3Client = new
                S3Client(getAccessKey(), getSecretKey(), getExistingBucketName(),
                getS3Url(), getS3Retries());

        environment.lifecycle().manage(new Managed() {
            @Override public void start() throws Exception {
                LOGGER.info("Starting lifecycle for S3Client");
            }

            @Override public void stop() throws Exception {
                LOGGER.info("Stopping lifecycle for S3Client");
            }
        });

        return s3Client;
    }

    /**
     * Getter for existingBucketName
     *
     * @return java.lang.String
     */
    @JsonProperty
    public String getExistingBucketName() {
        return existingBucketName;
    }

    /**
     * Setter for existingBucketName
     *
     * @param existingBucketName is a String with the name of the directory/bucket that images will be retrieved
     *                           and stored in.
     */
    @JsonProperty
    public void setExistingBucketName(String existingBucketName) {
        this.existingBucketName = existingBucketName;
    }

    /**
     * Getter for s3Url
     *
     * @return java.lang.String
     */
    @JsonProperty
    public String getS3Url() {
        return s3Url;
    }

    /**
     * Setter for s3Url
     *
     * @param s3Url is the full URL with the s3 host e.g. http://fake-s3:4569
     */
    @JsonProperty
    public void setS3Url(String s3Url) {
        this.s3Url = s3Url;
    }

    /**
     * Getter for accessKey
     *
     * @return java.lang.String
     */
    @JsonProperty
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * Setter for accessKey
     *
     * @param accessKey is the AWS key used to access S3. If you are using the fake-s3 service, then any value will work.
     */
    @JsonProperty
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     * Getter for secretKey
     *
     * @return java.lang.String
     */
    @JsonProperty
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Setter for secretKey
     *
     * @param secretKey is the AWS secret key sued to access s3. If you are using the fake-s3 service, then any value will work.
     */
    @JsonProperty
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Getter for s3Retries
     *
     * @return int
     */
    @JsonProperty
    public int getS3Retries() {
        return s3Retries;
    }

    /**
     * Setter for s3Retries
     *
     * @param s3Retries is the number of times that the s3 client will retry accessing the destination
     */
    @JsonProperty
    public void setS3Retries(int s3Retries) {
        this.s3Retries = s3Retries;
    }
}
