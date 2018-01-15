package com.nginx.image.core;

import org.apache.log4j.Logger;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

import java.io.*;
import java.util.HashMap;
import java.util.List;

/**
 * ExifManager.java
 * PhotoResizer
 *
 * The ExifManager class uses the sanselan library to manage the exchangeable
 * image file information when resizing images
 *
 * Copyright © 2017 NGINX Inc. All rights reserved.
 */
class ExifManager {

    // instantiate a logger
    private static final Logger logger = Logger.getLogger("com.nginx.image");

    /**
     * Static method which copies image data from one {@link File} to another
     *
     * @param sourceFile the file to copy from
     * @param destFile the file to copy to
     * @param excludedFields a list of {@link TagInfo} objects to exclude from being copied
     * @param updatedFields a {@link HashMap} of fields which should be updated in the
     *                      destination file
     */
    static void copyExifData(File sourceFile, File destFile, List<TagInfo> excludedFields,
                             HashMap<TagInfo, Integer> updatedFields) {

        // instantiate variables
        String tempFileName = destFile.getAbsolutePath() + ".tmp";
        File tempFile = null;
        OutputStream tempStream = null;

        try {
            tempFile = new File (tempFileName);

            TiffOutputSet sourceSet =
                    getSanselanOutputSet(sourceFile, TiffConstants.DEFAULT_TIFF_BYTE_ORDER);

            TiffOutputSet destSet =
                    getSanselanOutputSet(destFile, sourceSet.byteOrder);

            // If the EXIF data endianness of the source and destination files
            // differ then fail. This only happens if the source and
            // destination images were created on different devices. It's
            // technically possible to copy this data by changing the byte
            // order of the data, but handling this case is outside the scope
            // of this implementation
            if (sourceSet.byteOrder != destSet.byteOrder) {
                logger.debug("byteOrder is not the same.");
            }

            destSet.getOrCreateExifDirectory();
            sourceSet.getOrCreateExifDirectory();

            // Go through the source directories
            List<?> sourceDirectories = sourceSet.getDirectories();
            for (Object sourceDirectory1 : sourceDirectories) {
                TiffOutputDirectory sourceDirectory = (TiffOutputDirectory) sourceDirectory1;
                TiffOutputDirectory destinationDirectory = getOrCreateExifDirectory(destSet, sourceDirectory);

                if (destinationDirectory == null) {
                    continue;
                } // Failed to create

                // Loop the fields
                List<?> sourceFields = sourceDirectory.getFields();
                for (Object sourceField1 : sourceFields) {
                    // Get the source field
                    TiffOutputField sourceField = (TiffOutputField) sourceField1;

                    // Check exclusion list
                    if (excludedFields != null && excludedFields.contains(sourceField.tagInfo)) {
                        destinationDirectory.removeField(sourceField.tagInfo);
                        continue;
                        //This means that removing a filed takes precedence over updating a field
                    }

                    // Remove any existing field
                    destinationDirectory.removeField(sourceField.tagInfo);

                    if (updatedFields != null && updatedFields.containsKey(sourceField.tagInfo)) {
                        TiffOutputField updatedTag = TiffOutputField.create(
                                sourceField.tagInfo,
                                destSet.byteOrder, updatedFields.get(sourceField.tagInfo));
                        destinationDirectory.add(updatedTag);
                        continue;
                    }

                    // Add field
                    destinationDirectory.add(sourceField);
                }
            }

            // Save data to destination
            tempStream = new BufferedOutputStream(new FileOutputStream(tempFile));
            new ExifRewriter().updateExifMetadataLossless(destFile, tempStream, destSet);
            tempStream.close();

            // Replace file
            if (destFile.delete()) {
                tempFile.renameTo(destFile);
            }
        }
        catch (ImageReadException | ImageWriteException | IOException exception) {
            exception.printStackTrace();
        } finally {
            if (tempStream != null) {
                try {
                    tempStream.close();
                }
                catch (IOException ignored) {}
            }

            if (tempFile != null) {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }
    }

    /**
     * Gets the Sanselan output set from the file using the specified byte order
     *
     * @param jpegImageFile the image from which to extract the Sanselan output
     * @param defaultByteOrder the byte order
     * @return a {@link TiffOutputSet} object
     *
     * @throws IOException thrown from {@link Sanselan#getMetadata(File)}
     * @throws ImageReadException thrown from {@link Sanselan#getMetadata(File)}
     * @throws ImageWriteException thrown from {@link Sanselan#getMetadata(File)}
     */
    private static TiffOutputSet getSanselanOutputSet(File jpegImageFile, int defaultByteOrder)
            throws IOException, ImageReadException, ImageWriteException {
        TiffImageMetadata exif = null;
        TiffOutputSet outputSet = null;

        JpegImageMetadata metadata = (JpegImageMetadata) Sanselan.getMetadata(jpegImageFile);
        if (metadata != null) {
            exif = metadata.getExif();

            if (exif != null) {
                outputSet = exif.getOutputSet();
            }
        }

        // If JPEG file contains no EXIF metadata, create an empty set
        // of EXIF metadata. Otherwise, use existing EXIF metadata to
        // keep all other existing tags
        if (outputSet == null) {
            outputSet = new TiffOutputSet(exif == null ? defaultByteOrder : exif.contents.header.byteOrder);
        }

        return outputSet;
    }

    /**
     * Checks whether a {@link TiffOutputDirectory} exists for the {@link TiffOutputSet}, if so that
     * directory is returned. If not, a new one is created.
     *
     * @param outputSet the {@link TiffOutputSet} to check for the directory
     * @param outputDirectory the {@link TiffOutputDirectory} to check for
     * @return a {@link TiffOutputDirectory}
     */
    private static TiffOutputDirectory getOrCreateExifDirectory(TiffOutputSet outputSet, TiffOutputDirectory outputDirectory) {
        TiffOutputDirectory result = outputSet.findDirectory(outputDirectory.type);
        if (result != null) {
            return result;
        }
        result = new TiffOutputDirectory(outputDirectory.type);
        try {
            outputSet.addDirectory(result);
        } catch (ImageWriteException e) {
            return null;
        }
        return result;
    }
}
