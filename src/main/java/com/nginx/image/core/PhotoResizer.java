package com.nginx.image.core;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.nginx.image.PhotoResizerConfiguration;
import io.dropwizard.jetty.MutableServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffImageData;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;

import javax.imageio.*;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.MalformedInputException;
import java.nio.file.FileSystemException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.round;

/**
 * Created by cstetson on 10/24/15.
 * Copyright (C) 2015 Nginx, Inc.
 */
public class PhotoResizer
{
    private BufferedImage imageToResize;
    private BufferedImage mediumImage;
    private BufferedImage thumbImage;
    private final MutableServletContextHandler servletContext = new MutableServletContextHandler();
    private String imageURL;
    private BufferedImage originalBuffImage;
    private File originalImage;
    private int orientation;
    private int width = 0;
    private int height = 0;
    private final static String LARGE = PhotoResizerConfiguration.getLARGE();
    private final static String MEDIUM = PhotoResizerConfiguration.getMEDIUM();
    private final static String THUMB = PhotoResizerConfiguration.getTHUMB();
    private final static Integer LARGE_SIZE = PhotoResizerConfiguration.getLargeSize();//-1 means stay the same
    private final static Integer MEDIUM_SIZE = PhotoResizerConfiguration.getMediumSize();
    private final static Integer THUMB_SIZE = PhotoResizerConfiguration.getThumbSize();
    private final static ImmutableMap<String, Integer> sizesMap = PhotoResizerConfiguration.getSizesMap();
    private String keyBase;
    private Float compressionQuality = PhotoResizerConfiguration.getCompressionQuality();
    //private static RedisConnection<String, String> myRedis = createAWSElasticCacheClient();
    //private static ConcurrentHashMap<String,String> localCache = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoResizer.class);
    int s3ReAttempts = 0;
    String classInstance;


    public void PhotoResizer()
    {
    }

    public String resizeImage(String imageURL)
    {
        this.classInstance = " + class instance = " + System.identityHashCode(this);
        System.out.println("Start App: URL " + imageURL + classInstance);
        this.imageURL = imageURL;
        originalBuffImage = null;
        String resizedImagesMapAsJSON = "";
        ConcurrentHashMap<String,File> imageFilesMap;
        imageFilesMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String,String> imagesURLMap;
        imagesURLMap = new ConcurrentHashMap<>();

/*
        if(getCacheMap(this.imageURL) != null)
        {
                resizedImagesMapAsJSON = getCacheMap(this.imageURL);
                return resizedImagesMapAsJSON;
        }
*/
        try
        {
            URL jpgURL = new URL(imageURL);
            this.keyBase = jpgURL.getPath();
            this.keyBase = this.keyBase.replaceAll("original.*$","");
            System.out.println("Start Try: Keybase and URL: " + this.keyBase + " : " +imageURL + classInstance);
            //we need to use files because the Sanselan EXIF libraries expect it
            File repository = (File) Files.createTempDir();
            // Configure a repository (to ensure a secure temp location is used)
            this.originalImage = File.createTempFile(LARGE + "_", ".jpg", repository);
            //TODO: create file handle pools
            FileUtils.copyURLToFile(jpgURL, this.originalImage);//this retains the EXIF information
            originalBuffImage = ImageIO.read(this.originalImage);
            this.width = originalBuffImage.getWidth(null);
            this.height = originalBuffImage.getHeight(null);
            System.out.println("Start Files: Original ImagePath " + originalImage.getAbsolutePath() + " : " +imageURL + classInstance);


            this.transformOriginalImage();//this makes sure the originalImage is oriented correctly

            imageFilesMap.put(LARGE, this.originalImage);
            imageFilesMap.put(MEDIUM, File.createTempFile(MEDIUM + "_", ".jpg", repository));
            imageFilesMap.put(THUMB, File.createTempFile(THUMB + "_", ".jpg", repository));

            //execute these in parallel using lambda expressions and ConcurrentHashMap parallelism
            imageFilesMap.forEach(1, (size, imageFile) ->
            {
                resize(imageFile,sizesMap.get(size));

                String keyName = keyBase + size + ".jpg";
                System.out.println("Mid App: keyname " + keyName + classInstance);
                s3FileUpload(imageFilesMap.get(size),keyName);
                String uploadedURL = jpgURL.getProtocol() + "://" + jpgURL.getHost() + keyName;
                imagesURLMap.put(size,uploadedURL);
                //imageFilesMap.get(size).delete();
                imageFilesMap.remove(size);
                /*
                ADD This to imagesURLMAP
                t.string   "name"
                t.string   "description"
                t.datetime "created_at",    null: false
                t.datetime "updated_at",    null: false
                t.integer  "album_id"
                t.string   "url"
                t.string   "thumb_url"
                t.integer  "thumb_height"
                t.integer  "thumb_width"
                t.string   "medium_url"
                t.integer  "medium_height"
                t.integer  "medium_width"
                t.string   "large_url"
                t.integer  "large_height"
                t.integer  "large_width"
                 */
            });
        }
        catch (MalformedInputException e)
        {
            LOGGER.error("URL error: " + e.getMessage());
        }
        catch (FileSystemException e)
        {
            LOGGER.error("FileSystem error: " + e.getMessage());
        }
        catch (Exception e)
        {
            LOGGER.error("General error: " + e.getMessage());
        }
        finally
        {

/*
            for(File image:imageFilesMap.values())
            {
                image.delete();
            }
*/
            if(imageFilesMap.isEmpty())
            {
                //this way we make sure all the resize threads are done
                this.originalImage.delete();
                originalBuffImage.flush();
            }
        }
        resizedImagesMapAsJSON = makeJson(imagesURLMap);
        System.out.println("End App: JSON " + resizedImagesMapAsJSON + classInstance);
        //putCacheMap(imageURL,resizedImagesMapAsJSON);
        return resizedImagesMapAsJSON;
    }

    private String makeJson(ConcurrentHashMap<String,String> imagesURLMap)
    {
        /**
         * Map will contain
         * large -> <S3 URL>
         * medium -> <S3 URL>
         * small -> <S3 URL>
         */
        String imagesMapAsJSON = "";
        try
        {
            imagesMapAsJSON = new ObjectMapper().writeValueAsString(imagesURLMap);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return imagesMapAsJSON;
    }

    private void resize(File resizedImageFile, int maxSize)
    {
        BufferedImage resizedBuffImage;
        double scale = 0d;
        try
        {
            if(maxSize == -1)
            {
                scale = 1;
                return;
            }
            else if (width > height) //sizes[0] is width, [1] is HEIGHT
            {
                scale = (double) maxSize/originalBuffImage.getWidth();
            }
            else
            {
                scale = (double) maxSize/originalBuffImage.getHeight();
            }
            int widthScale = (int) round(scale * originalBuffImage.getWidth());
            int heightScale = (int) round(scale * originalBuffImage.getHeight());
            this.resize(resizedImageFile, widthScale, heightScale);
        }
        catch (Exception e)
        {
            LOGGER.debug("This is the general exception message: " + e.getMessage());
        }
    }

    private ImageInformation resize(File resizedImageFile, int width, int height)
    {
        BufferedImage resizedBuffImage = null;
        try
        {
            resizedBuffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Image tmp = this.originalBuffImage.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
            //Bicubic resize doesn't work well in Java -- scale_smooth gives photoshop-like resizing
            resizedBuffImage.getGraphics().drawImage(tmp, 0, 0, null);

/*
            JpegImageMetadata meta=((JpegImageMetadata) Sanselan.getMetadata(originalImage));
            TiffImageMetadata data=null;
            if (meta != null)
            {
                data=meta.getExif();
            }
            orientation=0;
            if (data != null)
            {
                orientation = data.findField(ExifTagConstants.EXIF_TAG_ORIENTATION).getIntValue();
            }
            AffineTransform t = getExifTransformation(new ImageInformation(orientation,width,height));
            resizedBuffImage = transformImage(resizedBuffImage,t);//orients the pixels to 1(normal)
*/
            writeJpg(resizedImageFile, resizedBuffImage, compressionQuality);
            resizedBuffImage.flush();
            tmp.flush();
        }
        catch (NullPointerException e)
        {
            LOGGER.debug("This is the null pointer exception message: " + e.getMessage() + "\n");
        }
/*
        catch (FileSystemException e)
        {
            LOGGER.debug("This is the FileSystem exception message: " + e.getMessage() + "\n");
        }
*/
        catch (Exception e)
        {
            LOGGER.debug("This is the general exception message: " + e.getMessage());
        }
        return new ImageInformation(1, width, height);
    }

    private void writeJpg(File fileHandle, BufferedImage resizedImage, float compressionQuality)
    {
        Iterator writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter resizeWriter = (ImageWriter)writers.next();

        JPEGImageWriteParam params = new JPEGImageWriteParam(null);

        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(compressionQuality);
        params.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
        params.setOptimizeHuffmanTables(true);
        params.setDestinationType(new ImageTypeSpecifier(IndexColorModel.getRGBdefault(), IndexColorModel.getRGBdefault().createCompatibleSampleModel(16,16)));


        try
        {
            ImageOutputStream ios = ImageIO.createImageOutputStream(fileHandle);
            ArrayList<TagInfo> excludes= new ArrayList<>();
			/*excludes.add(TiffConstants.TIFF_TAG_ORIENTATION);//clears the orientation tags
			excludes.add(TiffConstants.EXIF_TAG_ORIENTATION);//clears the orientation tags
			excludes.add(TiffConstants.EXIF_TAG_EXIF_IMAGE_WIDTH);//clears the width tags
			excludes.add(TiffConstants.EXIF_TAG_EXIF_IMAGE_LENGTH);//clears the hieght tags*/

            HashMap<TagInfo, Integer> tagUpdates = new HashMap<>();
            orientation = 1;
            tagUpdates.put(TiffConstants.TIFF_TAG_ORIENTATION,orientation);//sets the orientation tags
            tagUpdates.put(TiffConstants.EXIF_TAG_ORIENTATION,orientation);//sets the orientation tags
            tagUpdates.put(TiffConstants.EXIF_TAG_EXIF_IMAGE_WIDTH, resizedImage.getWidth(null));
            tagUpdates.put(TiffConstants.EXIF_TAG_EXIF_IMAGE_LENGTH, resizedImage.getHeight(null));

            resizeWriter.setOutput(ios);
            resizeWriter.write(null, new IIOImage(resizedImage, null, null), params);

            ExifManager.copyExifData(originalImage,fileHandle,excludes,tagUpdates);
            ios.close();
        }
        catch (IOException e)
        {
            LOGGER.debug("caught IOException while writing " + fileHandle.getPath());
            e.printStackTrace();
        }
    }

    private File transformOriginalImage()
    {
        try
        {
            JpegImageMetadata meta=((JpegImageMetadata) Sanselan.getMetadata(originalImage));
            TiffImageMetadata data=null;
            TiffImageData imageData = null;
            if (meta != null) {
                data = meta.getExif();
                imageData = meta.getRawImageData();
            }
            orientation=0;
            if (data != null) {
                orientation = data.findField(ExifTagConstants.EXIF_TAG_ORIENTATION).getIntValue();
                if(orientation == 1) return originalImage;
            }
            AffineTransform t = getExifTransformation(new ImageInformation(orientation,width,height));
            originalBuffImage = transformImage(originalBuffImage,t);
            writeJpg(originalImage, originalBuffImage, compressionQuality);
            originalBuffImage.flush();
        }
        catch (NullPointerException e)
        {
            LOGGER.debug("This is the File exception message: " + e.getMessage() + "\n");
        }
        catch (URISyntaxException e)
        {
            LOGGER.debug("This is the URI exception message: " + e.getMessage() + "\n");
        }
        catch (Exception e)
        {
            LOGGER.debug("This is the general exception message: " + e.getMessage());
        }
        return originalImage;
    }

    public static BufferedImage transformImage(BufferedImage image, AffineTransform transform) throws Exception {

        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

        BufferedImage destinationImage = op.createCompatibleDestImage(image, (image.getType() == BufferedImage.TYPE_BYTE_GRAY) ? image.getColorModel() : null);
        Graphics2D g = destinationImage.createGraphics();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
        destinationImage = op.filter(image, destinationImage);
        return destinationImage;
    }

    public static class ImageInformation //inner class
    {
        public final int orientation;
        public final int width;
        public final int height;

        public ImageInformation(int orientation, int width, int height)
        {
            this.orientation = orientation;
            this.width = width;
            this.height = height;
        }

        public String toString()
        {
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

    private boolean s3FileUpload(File fileToUpload,String keyName)
    {

        String existingBucketName = PhotoResizerConfiguration.getS3BucketName();
        //this should convert http(s)://blah.aws.com/<some extended url>original.jpg"
        //to <some extended url>

        TransferManager tm = new TransferManager(new EnvironmentVariableCredentialsProvider());

        try
        {
            // TransferManager processes all transfers asynchronously,
            // so this call will return immediately.
            keyName = keyName.replaceFirst("^/","");//this is because the original key should not have a starting slash
            Upload upload = tm.upload(existingBucketName, keyName, fileToUpload);
            // You can poll your transfer's status to check its progress
            if (upload.isDone() == false) {
                System.out.println("Transfer: " + upload.getDescription());
                System.out.println("  - State: " + upload.getState());
                System.out.println("  - Progress: "
                        + upload.getProgress().getBytesTransferred());
            }

            // Transfers also allow you to set a ProgressListener to receive
            // asynchronous notifications about your transfer's progress.

            upload.waitForCompletion();
            if(upload.isDone())
            {
                System.out.println("Transfer: " + upload.getDescription());
                System.out.println("Upload complete.");
            }
        }
        catch (AmazonClientException amazonClientException)
        {
            boolean madeit = false;
            if (s3ReAttempts < 3)
            {
                s3ReAttempts++;
                LOGGER.error("Struggling to upload file: Attempt" + s3ReAttempts);
                madeit = s3FileUpload(fileToUpload, keyName);
            }
            LOGGER.error("Unable to upload file, upload was aborted:" + amazonClientException.getMessage());
            amazonClientException.printStackTrace();
            return madeit;
        }
        catch (InterruptedException e)
        {
            LOGGER.error("Unable to upload file, upload was aborted:" + e.getMessage());
            e.printStackTrace();
            return false;
        }
        finally
        {
            // After the upload is complete, call shutdownNow to release the resources.
            tm.shutdownNow();
        }
        return true;
    }

    /*private static RedisConnection createAWSElasticCacheClient()
    {
        AmazonElastiCacheClient awsECC = new AmazonElastiCacheClient(new EnvironmentVariableCredentialsProvider());
        RedisClient redisCache = new RedisClient(PhotoResizerConfiguration.getRedisCacheUrl(),PhotoResizerConfiguration.getRedisCachePort());
        RedisConnection<String, String> myRedis = null;
        myRedis.setTimeout(new Long(30), TimeUnit.SECONDS);
        try
        {
            myRedis = redisCache.connect();
        }
        catch (RedisConnectionException e)
        {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            myRedis = null;
        }
        catch (Exception e)
        {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            myRedis = null;
        }
        finally
        {
            return myRedis;
        }
    }

    private String getCacheMap(String mapURL)
    {
        String resizedImagesMapAsJSON = null;
        if(myRedis != null && myRedis.get(mapURL) != null)
        {
            resizedImagesMapAsJSON = myRedis.get(mapURL);
        }
        else if(!localCache.isEmpty() && localCache.get(mapURL) != null)
        {
            resizedImagesMapAsJSON = localCache.get(mapURL);
        }
        return resizedImagesMapAsJSON;
    }

    private void putCacheMap(String mapURL, String resizedImagesMapAsJSON)
    {
        if(myRedis != null && myRedis.get(mapURL) != null)
        {
            myRedis.set(mapURL, resizedImagesMapAsJSON);
        }
        else
        {
            localCache.put(mapURL,resizedImagesMapAsJSON);
        }
    }
*/
}

