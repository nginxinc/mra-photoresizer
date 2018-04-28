package com.nginx.image.core;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.transfer.Download;
import com.google.common.io.Files;
import com.nginx.image.net.S3Client;
import com.nginx.image.util.ImageSizeEnum;
import com.nginx.image.util.ResizerException;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.*;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
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

/**
 * PhotoIO.java
 * PhotoIO
 *
 * Class that does the work of retrieving from a URL, saving to disk, uploading the image
 *
 * Copyright © 2018 NGINX Inc. All rights reserved.
 */
public class PhotoIO
{
    // The logger for this instance of the service
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoIO.class);

    // The string to use when displaying the class instance
    private final String classInstance = " class instance = " + System.identityHashCode(this);
    private final S3Client s3Client;

    /**
     * The constructor method takes an instantiated S3 client
     *
     * @param s3Client the name of the {@link S3Client}
     */
    public PhotoIO(S3Client s3Client)
    {
        this.s3Client = s3Client;
    }

    /**
     * This method writes a JPG file to the file system
     *
     * @param fileHandle the name of the {@link File}
     * @param resizedImage the resized {@link BufferedImage} to write
     * @param compressionQuality the compression quality to use when saving the image
     */
    public void writeJpg(File fileHandle, BufferedImage resizedImage, float compressionQuality) throws ResizerException
    {

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
            throw new ResizerException("Generic Exception while resizing", e);
        }
        catch (Exception e) {
            LOGGER.error("General error: ", e);
            throw new ResizerException("Generic Exception while resizing", e);
        }
    }

    /**
     * This method retrieves the image from a URL
     *
     * @param imageURL the name of the {@link String}
     *
     * @return a {@link ConcurrentHashMap<ImageSizeEnum,File>} is returned with 3 image files created corresponding to the ImageSizeEnum
     */
    public ConcurrentHashMap<ImageSizeEnum,File> getImage(String imageURL) throws ResizerException
    {
        try
        {
            // parse a URL from the imageURL parameter
            URL jpgURL = new URL(imageURL);
            String extension = jpgURL.toString().substring(jpgURL.toString().lastIndexOf("."));

            // create the baseImagePath by removing the file name from the jpgURL
            // http://s3.amazonaws.com/bucket-name/path/original.jpg becomes
            // http://s3.amazonaws.com/bucket-name/path/
            final String baseImagePath = jpgURL.getPath().replaceAll("original.*$", "");

            LOGGER.info("Start Try: Keybase and URL: " + baseImagePath + ": " + imageURL + classInstance);

            // We need to use files because the Sanselan EXIF libraries expect it
            File repository = Files.createTempDir();
            // Configure a repository (to ensure a secure temp location is used)
            File originalImageFile = File.createTempFile(ImageSizeEnum.LARGE.getSizeName() + "_", extension, repository);

            // http://s3.amazonaws.com/path/original.jpg
            Download originalDownload = s3Client.download(
                    baseImagePath.replace("/" + s3Client.getExistingBucketName() + "/", "") + "original" + extension,
                    originalImageFile);

            if (!originalDownload.isDone())
            {
                LOGGER.info("Transfer: " + originalDownload.getDescription());
                LOGGER.info("  - State: " + originalDownload.getState());
                LOGGER.info("  - Progress: " + originalDownload.getProgress().getBytesTransferred());
            }

            // Transfers also allow you to set a ProgressListener to receive
            // asynchronous notifications about your transfer's progress.
            originalDownload.waitForCompletion();

            LOGGER.info("Transfer: " + originalDownload.getDescription());
            LOGGER.info("Download complete. " + originalImageFile);

            ConcurrentHashMap<ImageSizeEnum,File> imageFilesMap = new ConcurrentHashMap<>();
            // store the images and the images in the imagesFileMap
            imageFilesMap.put(ImageSizeEnum.LARGE, originalImageFile);
            imageFilesMap.put(ImageSizeEnum.MEDIUM,
                    File.createTempFile(ImageSizeEnum.MEDIUM.getSizeName() + "_", extension, repository));
            imageFilesMap.put(ImageSizeEnum.THUMB,
                    File.createTempFile(ImageSizeEnum.THUMB.getSizeName() + "_", extension, repository));


            return imageFilesMap;
        }
        catch (MalformedInputException e) {
            LOGGER.error("URL error: ", e);
            throw new ResizerException("Invalid URL while resizing", e);
        }
        catch (FileSystemException e) {
            LOGGER.error("FileSystem error: ", e);
            throw new ResizerException("FileSystem Exception while resizing", e);
        }
        catch (AmazonServiceException e) {
            LOGGER.error("Amazon service error: " + e.getMessage());
            throw new ResizerException("Amazon service error: ", e);
        }
        catch (AmazonClientException e) {
            LOGGER.error("Amazon client error: " + e.getMessage());
            throw new ResizerException("Amazon client error: ", e);
        }
        catch (InterruptedException e) {
            LOGGER.error("Transfer interrupted: " + e.getMessage());
            throw new ResizerException("Transfer interrupted: ", e);
        }
        catch (Exception e) {
            LOGGER.error("General error: ", e);
            throw new ResizerException("Generic Exception while resizing", e);
        }
    }

    /**
     * This method uploads the image to S3
     *
     * @param imageFile the image that is being uploaded from a {@link File} to S3
     *
     */
    public void uploadImage(File imageFile, String imagePath)
    {
        // upload the file. TODO: this should call the uploader service
        s3Client.fileUpload(imageFile, imagePath);
    }
}
