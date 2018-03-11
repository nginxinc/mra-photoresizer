package com.nginx.image.net;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created 12/1/17 12:57 with IntelliJ IDEA.
 * User: charlespretzer
 */
public class S3Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3Client.class);

    // The TransferManager object used to upload and download images
    private final TransferManager transferManager;

    private final String existingBucketName;

    private final String accessKey;

    private final String secretKey;

    private final String s3Url;

    private final int s3Retries;

    // The bucket name to use when storing the images
    public S3Client(String accessKey, String secretKey, String existingBucketName,
            String s3Url, int s3Retries) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.s3Url = s3Url;
        this.s3Retries = s3Retries;
        this.existingBucketName = existingBucketName;

        // create the AWSCredentials
        AWSCredentials credentials = new BasicAWSCredentials(
                this.accessKey,
                this.secretKey);

        // create and configure the AmazonS3Client using the AWSCredentials
        AmazonS3Client s3Client = new AmazonS3Client(credentials);
        s3Client.setEndpoint(this.s3Url);
        s3Client.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));

        // instantiate the transferManager using the AmazonS3Client
        transferManager = new TransferManager(s3Client);

    }

    public Download download(String baseImagePath, File originalImage) {
        LOGGER.info("S3Client download: " + baseImagePath + ", file: " + originalImage);
        return transferManager.download(existingBucketName, baseImagePath, originalImage);
    }


    /**
     * Uploads a file to S3 using the {@link AmazonS3Client}. In the event of upload
     * failure, this method will be called recursively as many times as is specified in the
     * the s3ReAttempts variable
     *
     * TODO: This should make a call to the uploader service to separate functionality
     *
     * @param fileToUpload the file which will be uploaded
     * @param keyName the file URL
     *
     * @return boolean: true if the file was uploaded, false otherwise
     */
    public boolean fileUpload(File fileToUpload,String keyName) {

        int retries = 0;
        try {
            // TransferManager processes all transfers asynchronously, so this call will return immediately.
            // Sometimes the URL's come in with the bucketname to start with

            keyName = keyName.replaceFirst("^/" + existingBucketName,"");
            // This is because the original key should not have a starting slash
            keyName = keyName.replaceFirst("^/", "");
            Upload upload = transferManager.upload(existingBucketName, keyName, fileToUpload);

            // You can poll your transfer's status to check its progress
            if (!upload.isDone()) {
                LOGGER.info("Transfer: " + upload.getDescription());
                LOGGER.info("  - State: " + upload.getState());
                LOGGER.info("  - Progress: " + upload.getProgress().getBytesTransferred());
            }

            // Transfers also allow you to set a ProgressListener to receive
            // asynchronous notifications about your transfer's progress.
            upload.waitForCompletion();
            if(upload.isDone()) {
                LOGGER.info("Transfer: " + upload.getDescription());
                LOGGER.info("Upload complete.");
            }
        } catch (AmazonClientException amazonClientException) {
            boolean uploaded = false;
            if (retries < s3Retries) {
                retries++;
                LOGGER.error("Struggling to upload file: Attempt" + retries);
                uploaded = fileUpload(fileToUpload, keyName);
            }

            LOGGER.error("Unable to upload file, upload was aborted:" + amazonClientException.getMessage());
            amazonClientException.printStackTrace();
            return uploaded;
        }
        catch (InterruptedException e) {
            LOGGER.error("Unable to upload file, upload was aborted:", e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Getter for existingBucketName
     *
     * @return java.lang.String
     */
    public String getExistingBucketName() {
        return existingBucketName;
    }
}
