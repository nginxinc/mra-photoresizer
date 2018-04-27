package com.nginx.image.core;

import com.nginx.image.core.PhotoTransformer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.Download;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.nginx.image.configs.PhotoResizerConfiguration;
import com.nginx.image.net.S3Client;
import com.nginx.image.util.ImageSizeEnum;
import com.nginx.image.util.ResizerException;
import org.apache.sanselan.ImageReadException;
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
 * PhotoResizer.java
 * PhotoResizer
 *
 * Class that does the work of resizing and uploading an image
 *
 * Copyright © 2018 NGINX Inc. All rights reserved.
 */
public class PhotoResizer {

    // The logger for this instance of the service
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoResizer.class);

    // The string to use when displaying the class instance
    private final String classInstance = " class instance = " + System.identityHashCode(this);

    // The compression quality to use when resizing images
    private final PhotoResizerConfiguration config;
    private final S3Client s3Client;

    /**
     * Default no-arg constructor
     *
     * Initializes the {@link AmazonS3Client} using {@link AWSCredentials} generated from the
     * environment variables set in the {@link PhotoResizerConfiguration}
     *
     * @param s3Client The client needed to retrieve and store images
     * @param configuration the configuration object needed for resize and compression values
     */
    public PhotoResizer(S3Client s3Client, PhotoResizerConfiguration configuration)
    {
        this.s3Client = s3Client;
        this.config = configuration;
    }

    /**
     * Work method which downloads and begins the process of resizing the image specified
     * in the imageURL parameter. The actual resizing is done in the {@link #resize(File, int, BufferedImage)}
     * method
     *
     * @param imageURL a String indicating the location of the original image to resize
     *
     * @return a JSON String which represents a map of the resized image locations
     *
     * @throws ResizerException telling you what happened.
     */
    public String resizeImage(String imageURL) throws ResizerException {

        LOGGER.info("Start App: URL " + imageURL + classInstance);

        // initialize return value
        String resizedImagesMapAsJSON;

        // create maps to store the resized images and their URLs
        ConcurrentHashMap<ImageSizeEnum,File> imageFilesMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String,String> imagesURLMap = new ConcurrentHashMap<>();

        try {

            // parse a URL from the imageURL parameter
            URL jpgURL = new URL(imageURL);
            String extension = jpgURL.toString().substring(jpgURL.toString().lastIndexOf("."));

            // create the baseImagePath by removing the file name from the jpgURL
            // http://s3.amazonaws.com/bucket-name/path/original.jpg becomes
            // http://s3.amazonaws.com/bucket-name/path/
            final String baseImagePath = jpgURL.getPath().replaceAll("original.*$","");

            LOGGER.info("Start Try: Keybase and URL: " + baseImagePath + ": " + imageURL + classInstance);

            // We need to use files because the Sanselan EXIF libraries expect it
            File repository = Files.createTempDir();
            // Configure a repository (to ensure a secure temp location is used)
            File originalImage = File.createTempFile(ImageSizeEnum.LARGE.getSizeName() + "_", extension, repository);

            // http://s3.amazonaws.com/path/original.jpg
            Download originalDownload = s3Client.download(
                    baseImagePath.replace("/" + s3Client.getExistingBucketName() + "/", "") + "original" + extension,
                    originalImage);

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
                LOGGER.info("Download complete. " + originalImage);

                // read the downloaded image in to a BufferedImage and get the dimensions
                BufferedImage originalBuffImage = ImageIO.read(originalImage);
                LOGGER.info("Generated originalBuffImage: " + originalBuffImage);
                if (originalBuffImage != null) {
                    int width = originalBuffImage.getWidth(null);
                    int height = originalBuffImage.getHeight(null);
                    LOGGER.info("Start Files: Original ImagePath " + originalImage.getAbsolutePath() + " : " +imageURL + classInstance);

                    // This makes sure the originalImage is oriented correctly
                    BufferedImage finalOriginalBuffImage  = new PhotoTransformer(width, height, originalImage, originalBuffImage);

                    // store the images and the images in the imagesFileMap
                    imageFilesMap.put(ImageSizeEnum.LARGE, originalImage);
                    imageFilesMap.put(ImageSizeEnum.MEDIUM,
                            File.createTempFile(ImageSizeEnum.MEDIUM.getSizeName() + "_", extension, repository));
                    imageFilesMap.put(ImageSizeEnum.THUMB,
                            File.createTempFile(ImageSizeEnum.THUMB.getSizeName() + "_", extension, repository));

                    // Execute these in parallel using lambda expressions and ConcurrentHashMap parallelism
                        imageFilesMap.forEach(1, (size, imageFile) -> {
                            try{
                                ImageInformation imageData = resize(imageFile, config.getImageSizeConfiguration().getImageSize(size), finalOriginalBuffImage);
                                String keyName = baseImagePath + size.getSizeName() + extension;
                                LOGGER.info("Mid App: keyname " + keyName + classInstance);

                                // upload the file. TODO: this should call the uploader service
                                s3Client.fileUpload(imageFile, keyName);
                                String uploadedURL = jpgURL.getProtocol() + "://" + jpgURL.getHost() + extractPort(jpgURL) + keyName;
                                imagesURLMap.put(size.getSizeName() + "_url",uploadedURL);
                                imagesURLMap.put(size.getSizeName() + "_height", String.valueOf(imageData.height));
                                imagesURLMap.put(size.getSizeName() + "_width", String.valueOf(imageData.width));
                            }
                            catch (Exception e)
                            {
                                LOGGER.error("Caught exception during resize for file " + imageFile +
                                        " with maxSize " + config.getImageSizeConfiguration().getImageSize(size), e);
                            }
                        });
                }
            }
        }
        catch (MalformedInputException e) {
            LOGGER.error("URL error: ", e);
            throw new ResizerException("Invalid URL while resizing", e);
        }
        catch (FileSystemException e) {
            LOGGER.error("FileSystem error: ", e);
            throw new ResizerException("FileSystem Exception while resizing", e);
        }
        catch (Exception e) {
            LOGGER.error("General error: ", e);
            throw new ResizerException("Generic Exception while resizing", e);
        } finally {
            for(ImageSizeEnum image:imageFilesMap.keySet()) {
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

    /**
     * Helper method which calls {@link ObjectMapper#writeValueAsString(Object)} to print
     * the image URLs to a JSON String
     *
     * @param imagesURLMap the {@link ConcurrentHashMap} which contains the URLs to
     *                     transform to JSON
     *
     * @return a JSON String
     */
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

    /**
     * Helper method which resizes an image
     *
     * @param resizedImageFile The file to store the image
     * @param maxSize the maximum size of the image to use when creating the scaling dimensions
     * @param originalBuffImage The image to resize
     *
     * @return an {@link ImageInformation} object
     */
    private ImageInformation resize(File resizedImageFile, int maxSize, BufferedImage originalBuffImage) throws Exception {
        try {

            double scale;

            // maxSize == -1 means that the original size should be used
            if(maxSize != -1) {
                if (originalBuffImage.getWidth() > originalBuffImage.getHeight()) {
                    // if the image is wider than it is tall, then scale base on width
                    scale = (double) maxSize/originalBuffImage.getWidth();
                } else {
                    // if the image is taller than it is wide, then scale base on height
                    scale = (double) maxSize/originalBuffImage.getHeight();
                }
                // get the scaling dimensions
                int widthScale = (int) round(scale * originalBuffImage.getWidth());
                int heightScale = (int) round(scale * originalBuffImage.getHeight());

                BufferedImage resizedBuffImage;
                resizedBuffImage = new BufferedImage(widthScale, heightScale, BufferedImage.TYPE_INT_RGB);
                Image tmp = originalBuffImage.getScaledInstance(widthScale, heightScale, BufferedImage.SCALE_SMOOTH);
                resizedBuffImage.getGraphics().drawImage(tmp, 0, 0, null);
                originalBuffImage = resizedBuffImage;
                resizedBuffImage.flush();
                tmp.flush();
            }
            ImageInformation imageData = new ImageInformation(1, originalBuffImage.getWidth(), originalBuffImage.getHeight());
            writeJpg(resizedImageFile, originalBuffImage, this.config.getResizerConfiguration().getCompressionQuality());
            return imageData;

        } catch (Exception e) {
            LOGGER.error("Caught exception during resize for file " + resizedImageFile +
                    " with maxSize " + maxSize, e);
            throw e;
        }
    }


    /**
     * This method writes a JPG file to the file system
     *
     * @param fileHandle the name of the file
     * @param resizedImage the resized image to write
     * @param compressionQuality the compression quality to use when saving the image
     */
    private void writeJpg(File fileHandle, BufferedImage resizedImage, float compressionQuality) {

        // instantiate ImageWriter and JPEGImageWriteParam instances
        Iterator writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter resizeWriter = (ImageWriter)writers.next();
        JPEGImageWriteParam params = new JPEGImageWriteParam(null);

        // configure the JPEGImageWriteParams
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

    /**
     * Inner class used to store metadata about an image:
     * - orientation
     * - width
     * - height
     *
     */
    public static class ImageInformation {
        final int orientation;
        final int width;
        final int height;

        /**
         * Constructor
         *
         * @param orientation the orientation as an int
         * @param width the width as an int
         * @param height the height as an int
         */
        ImageInformation(int orientation, int width, int height) {
            this.orientation = orientation;
            this.width = width;
            this.height = height;
        }

        /**
         * Overrides the {@link Object#toString()}
         *
         * @return the class properties as a String
         */
        @Override
        public String toString() {
            return String.format("%dx%d,%d", this.width, this.height, this.orientation);
        }
    }
}

