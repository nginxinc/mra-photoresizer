package com.nginx.image.resources;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.PersistableDownload;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.exception.PauseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * Created 12/4/17 18:09 with IntelliJ IDEA.
 * User: charlespretzer
 */

public class MockDownload implements Download {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MockDownload.class);

    @Override
    public ObjectMetadata getObjectMetadata() {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("test-metakey", "test-metavalue");
        return metadata;
    }

    @Override
    public String getBucketName() {
        return "test-bucket";
    }

    @Override
    public String getKey() {
        return "test-key";
    }

    @Override
    public void abort() throws IOException {
        LOGGER.info("MockDownload.abort called");
    }

    @Override
    public PersistableDownload pause() throws PauseException {
        LOGGER.info("MockDownload.pause called");
        return null;
    }

    @Override
    public boolean isDone() {
        LOGGER.info("MockDownload.isDone called");
        return true;
    }

    @Override
    public void waitForCompletion()
            throws AmazonClientException, AmazonServiceException,
            InterruptedException {
        LOGGER.info("MockDownload.waitForCompletion called");
    }

    @Override
    public AmazonClientException waitForException()
            throws InterruptedException {
        LOGGER.info("MockDownload.waitForException called");
        return null;
    }

    @Override
    public String getDescription() {
        return "MockDownload.getDescription implementation ";
    }

    @Override
    public TransferState getState() {
        LOGGER.info("MockDownload.getState called");
        return null;
    }

    @Override
    public void addProgressListener(
            ProgressListener progressListener) {
        LOGGER.info("MockDownload.addProgressListener called");
    }

    @Override
    public void removeProgressListener(
            ProgressListener progressListener) {
        LOGGER.info("MockDownload.removeProgressListener called");

    }

    @Override
    public TransferProgress getProgress() {
        LOGGER.info("MockDownload.getProgress called");
        return null;
    }

    @Override public void addProgressListener(
            com.amazonaws.services.s3.model.ProgressListener progressListener) {
        LOGGER.info("MockDownload.addProgressListener called");

    }

    @Override public void removeProgressListener(
            com.amazonaws.services.s3.model.ProgressListener progressListener) {
        LOGGER.info("MockDownload.removeProgressListener called");
    }
}
