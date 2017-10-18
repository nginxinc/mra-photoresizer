package com.nginx.image.core;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.nginx.image.PhotoResizerConfiguration;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.MalformedInputException;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.round;

/**
 //  PhotoResizer.java
 //  PhotoResizer
 //
 //  Copyright © 2017 NGINX Inc. All rights reserved.
 */


public class PhotoResizer {
//    private BufferedImage originalBuffImage;
//    private File originalImage;
//    private int orientation;
//    private int width = 0;
//    private int height = 0;
    private final static String LARGE = PhotoResizerConfiguration.getLARGE();
    private final static String MEDIUM = PhotoResizerConfiguration.getMEDIUM();
    private final static String THUMB = PhotoResizerConfiguration.getTHUMB();
    private final static ImmutableMap<String, Integer> sizesMap = PhotoResizerConfiguration.getSizesMap();
    private final Float compressionQuality = PhotoResizerConfiguration.getCompressionQuality();
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoResizer.class);
    private int s3ReAttempts = 0;
    private final TransferManager transferManager;
    private final String existingBucketName;
    private String classInstance;


    public PhotoResizer() {
        AWSCredentials credentials = new BasicAWSCredentials(PhotoResizerConfiguration.getAccessKey(),
                PhotoResizerConfiguration.getSecretKey());
        AmazonS3Client s3Client = new AmazonS3Client(credentials);
        s3Client.setEndpoint(PhotoResizerConfiguration.getFakeS3URL());
        s3Client.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));

        transferManager = new TransferManager(s3Client);
        existingBucketName = PhotoResizerConfiguration.getS3BucketName();

    }

    /*
     * Example URL: https://s3-us-west-1.amazonaws.com/ngra-images/tests/photoresizer/12345/original.jpg
     */

    public String resizeImage(String imageURL) {
        this.classInstance = " + class instance = " + System.identityHashCode(this);
        LOGGER.info("Start App: URL " + imageURL + classInstance);

        String resizedImagesMapAsJSON;
        ConcurrentHashMap<String,File> imageFilesMap;
        imageFilesMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String,String> imagesURLMap;
        imagesURLMap = new ConcurrentHashMap<>();

        try {
            URL jpgURL = new URL(imageURL);
            final String keyBase = jpgURL.getPath().replaceAll("original.*$","");
            LOGGER.info("Start Try: Keybase and URL: " + keyBase + " : " + imageURL + classInstance);
            // We need to use files because the Sanselan EXIF libraries expect it
            File repository = Files.createTempDir();
            // Configure a repository (to ensure a secure temp location is used)
            File originalImage = File.createTempFile(LARGE + "_", ".jpg", repository);

            Download originalDownload = transferManager.download(existingBucketName,
                    keyBase.replace("/" + PhotoResizerConfiguration.getS3BucketName() + "/", "") + "original.jpg", originalImage);

            if (!originalDownload.isDone()) {
                LOGGER.info("Transfer: " + originalDownload.getDescription());
                LOGGER.info("  - State: " + originalDownload.getState());
                LOGGER.info("  - Progress: " + originalDownload.getProgress().getBytesTransferred());
            }

            // Transfers also allow you to set a ProgressListener to receive
            // asynchronous notifications about your transfer's progress.

            originalDownload.waitForCompletion();
            if(originalDownload.isDone()) {
                LOGGER.info("Transfer: " + originalDownload.getDescription());
                LOGGER.info("Download complete.");
                BufferedImage originalBuffImage = ImageIO.read(originalImage);
                int width = originalBuffImage.getWidth(null);
                int height = originalBuffImage.getHeight(null);
                LOGGER.info("Start Files: Original ImagePath " + originalImage.getAbsolutePath() + " : " +imageURL + classInstance);

                this.transformOriginalImage(width, height, originalImage, originalBuffImage); // This makes sure the originalImage is oriented correctly

                imageFilesMap.put(LARGE, originalImage);
                imageFilesMap.put(MEDIUM, File.createTempFile(MEDIUM + "_", ".jpg", repository));
                imageFilesMap.put(THUMB, File.createTempFile(THUMB + "_", ".jpg", repository));

                // Execute these in parallel using lambda expressions and ConcurrentHashMap parallelism
                imageFilesMap.forEach(1, (size, imageFile) -> {
                    ImageInformation imageData = resize(imageFile,sizesMap.get(size), originalBuffImage);

                    String keyName = keyBase + size + ".jpg";
                    LOGGER.info("Mid App: keyname " + keyName + classInstance);
                    s3FileUpload(imageFilesMap.get(size),keyName);
                    String uploadedURL = jpgURL.getProtocol() + "://" + jpgURL.getHost() + extractPort(jpgURL) + keyName;
                    imagesURLMap.put(size + "_url",uploadedURL);
                    imagesURLMap.put(size + "_height", String.valueOf(imageData.height));
                    imagesURLMap.put(size + "_width", String.valueOf(imageData.width));
                });

            }
        }
        catch (MalformedInputException e) {
            LOGGER.error("URL error: ", e);
        }
        catch (FileSystemException e) {
            LOGGER.error("FileSystem error: ", e);
        }
        catch (Exception e) {
            LOGGER.error("General error: ", e);
        } finally {
            for(String image:imageFilesMap.keySet()) {
                imageFilesMap.get(image).delete();
                imageFilesMap.remove(image);
            }
        }
        resizedImagesMapAsJSON = makeJson(imagesURLMap);
        LOGGER.info("End App: JSON " + resizedImagesMapAsJSON + classInstance);
        return resizedImagesMapAsJSON;
    }

    /**
     * Takes a URL and returns a String in the format ":<port-number>" when the port is
     * not equal to -1, 80, and 443.
     *
     * When the port is not set, {@link java.net.URL#getPort()} returns -1. It's probably
     * not necessary to check for ports 80 and 443 since they are the defaults for the
     * http and https schemes.
     *
     * There is no validation on the value of the port to ensure that it conforms to expected
     * values. Examples are greater than 0 and not a reserved port
     *
     * @param url an instance of {@link java.net.URL}
     * @return a String
     */
    private String extractPort(URL url) {
        String ret = "";

        if (url.getPort() != -1 && url.getPort() != 80 && url.getPort() != 443) {
            ret = ":" + Integer.toString(url.getPort());
        }

        return ret;
    }

    private String makeJson(ConcurrentHashMap<String,String> imagesURLMap) {
        /*
         * Map will contain
         * large -> <S3 URL>
         * medium -> <S3 URL>
         * small -> <S3 URL>
         */
        String imagesMapAsJSON = "";
        try {
            imagesMapAsJSON = new ObjectMapper().writeValueAsString(imagesURLMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return imagesMapAsJSON;
    }

    private ImageInformation resize(File resizedImageFile, int maxSize, BufferedImage originalBuffImage) {
        ImageInformation imageData = new ImageInformation(1,0,0);
        try {
            double scale;
            if(maxSize == -1) {
                imageData = new ImageInformation(1, originalBuffImage.getWidth(), originalBuffImage.getHeight());
                return imageData;
            }
            else if (originalBuffImage.getWidth() > originalBuffImage.getHeight()) { //sizes[0] is width, [1] is HEIGHT
                scale = (double) maxSize/originalBuffImage.getWidth();
            }
            else {
                scale = (double) maxSize/originalBuffImage.getHeight();
            }
            int widthScale = (int) round(scale * originalBuffImage.getWidth());
            int heightScale = (int) round(scale * originalBuffImage.getHeight());
            return this.resize(resizedImageFile, widthScale, heightScale, originalBuffImage);
        } catch (Exception e) {
            LOGGER.error("Caught exception during resize for file " + resizedImageFile +
                    " with maxSize " + maxSize, e);
        }
        return imageData;
    }

    private ImageInformation resize(File resizedImageFile, int width, int height, BufferedImage originalBuffImage) {
        BufferedImage resizedBuffImage;
        try {
            resizedBuffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Image tmp = originalBuffImage.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
            resizedBuffImage.getGraphics().drawImage(tmp, 0, 0, null);

            writeJpg(resizedImageFile, resizedBuffImage, compressionQuality);
            resizedBuffImage.flush();
            tmp.flush();
        } catch (Exception e) {
            LOGGER.error("This is the general exception message: ", e);
        }
        return new ImageInformation(1, width, height);
    }

    private void writeJpg(File fileHandle, BufferedImage resizedImage, float compressionQuality) {
        Iterator writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter resizeWriter = (ImageWriter)writers.next();

        JPEGImageWriteParam params = new JPEGImageWriteParam(null);

        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(compressionQuality);
        params.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
        params.setOptimizeHuffmanTables(true);
        params.setDestinationType(new ImageTypeSpecifier(IndexColorModel.getRGBdefault(), IndexColorModel.getRGBdefault().createCompatibleSampleModel(16,16)));


        try {
            ImageOutputStream ios = ImageIO.createImageOutputStream(fileHandle);
            ArrayList<TagInfo> excludes= new ArrayList<>();

            HashMap<TagInfo, Integer> tagUpdates = new HashMap<>();
            int orientation = 1;
            tagUpdates.put(TiffConstants.TIFF_TAG_ORIENTATION,orientation); // Sets the orientation tags
            tagUpdates.put(TiffConstants.EXIF_TAG_ORIENTATION,orientation); // Sets the orientation tags
            tagUpdates.put(TiffConstants.EXIF_TAG_EXIF_IMAGE_WIDTH, resizedImage.getWidth(null));
            tagUpdates.put(TiffConstants.EXIF_TAG_EXIF_IMAGE_LENGTH, resizedImage.getHeight(null));

            resizeWriter.setOutput(ios);
            resizeWriter.write(null, new IIOImage(resizedImage, null, null), params);

            ExifManager.copyExifData(fileHandle,fileHandle,excludes,tagUpdates);
            ios.close();
        }
        catch (IOException e) {
            LOGGER.error("caught IOException while writing " + fileHandle.getPath());
            e.printStackTrace();
        }
    }

    private void transformOriginalImage(int width, int height, File originalImage, BufferedImage originalBuffImage) {
        try {
            JpegImageMetadata meta=((JpegImageMetadata) Sanselan.getMetadata(originalImage));
            TiffImageMetadata data=null;
            if (meta != null) {
                data = meta.getExif();
            }
            int orientation = 0;
            if (data != null && data.findField(ExifTagConstants.EXIF_TAG_ORIENTATION) != null) {
                orientation = data.findField(ExifTagConstants.EXIF_TAG_ORIENTATION).getIntValue();
                if(orientation == 1) return;
            }
            AffineTransform t = getExifTransformation(new ImageInformation(orientation,width,height));
            originalBuffImage = transformImage(originalBuffImage,t);
            writeJpg(originalImage, originalBuffImage, compressionQuality);
            originalBuffImage.flush();
        } catch (Exception e) {
            LOGGER.error("This is the general exception message: ", e);
        }
    }

    private static BufferedImage transformImage(BufferedImage image, AffineTransform transform) {
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

        BufferedImage destinationImage = op.createCompatibleDestImage(image, (image.getType() == BufferedImage.TYPE_BYTE_GRAY) ? image.getColorModel() : null);
        Graphics2D g = destinationImage.createGraphics();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
        destinationImage = op.filter(image, destinationImage);
        return destinationImage;
    }

    public static class ImageInformation { // Inner class
        final int orientation;
        final int width;
        final int height;

        ImageInformation(int orientation, int width, int height) {
            this.orientation = orientation;
            this.width = width;
            this.height = height;
        }

        public String toString() {
            return String.format("%dx%d,%d", this.width, this.height, this.orientation);
        }
    }

    private static AffineTransform getExifTransformation(ImageInformation info) {
        AffineTransform t = new AffineTransform();

        switch (info.orientation) {
            case 1:
                break;
            case 2: // Flip X
                t.scale(-1.0, 1.0);
                t.translate(-info.width, 0);
                break;
            case 3: // PI rotation
                t.translate(info.width, info.height);
                t.rotate(Math.PI);
                break;
            case 4: // Flip Y
                t.scale(1.0, -1.0);
                t.translate(0, -info.height);
                break;
            case 5: // - PI/2 and Flip X
                t.rotate(-Math.PI / 2);
                t.scale(-1.0, 1.0);
                break;
            case 6: // -PI/2 and -width
                t.translate(info.height, 0);
                t.rotate(Math.PI / 2);
                break;
            case 7: // PI/2 and Flip
                t.scale(-1.0, 1.0);
                t.translate(-info.height, 0);
                t.translate(0, info.width);
                t.rotate(  3 * Math.PI / 2);
                break;
            case 8: // PI / 2
                t.translate(0, info.width);
                t.rotate(  3 * Math.PI / 2);
                break;
        }
        return t;
    }

    private boolean s3FileUpload(File fileToUpload,String keyName) {


        try {
            // TransferManager processes all transfers asynchronously, so this call will return immediately.
            keyName = keyName.replaceFirst("^/" + existingBucketName,""); // Sometimes the URL's come in with the bucketname to start with
            keyName = keyName.replaceFirst("^/", ""); // This is because the original key should not have a starting slash
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
        }
        catch (AmazonClientException amazonClientException) {
            boolean madeIt = false;
            if (s3ReAttempts < 3) {
                s3ReAttempts++;
                LOGGER.error("Struggling to upload file: Attempt" + s3ReAttempts);
                madeIt = s3FileUpload(fileToUpload, keyName);
            }
            LOGGER.error("Unable to upload file, upload was aborted:" + amazonClientException.getMessage());
            amazonClientException.printStackTrace();
            return madeIt;
        }
        catch (InterruptedException e) {
            LOGGER.error("Unable to upload file, upload was aborted:", e);
            e.printStackTrace();
            return false;
        } finally {
            // After the upload is complete, call shutdownNow to release the resources.
//            transferManager.shutdownNow();
        }
        return true;
    }
}

