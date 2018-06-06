package com.nginx.image.configs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nginx.image.util.ImageSizeEnum;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.EnvironmentVariableLookup;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;

/**
 *  ResizerConfiguration.java
 *  PhotoResizer
 *
 *  Copyright © 2018 NGINX Inc. All rights reserved.
 *
 *  This class extends {@link Configuration}
 */

public class ImageSizeConfiguration extends Configuration {
    private static final EnvironmentVariableLookup echoEnv =
            new EnvironmentVariableLookup();

   /**
     * PhotoResizer ValueMap
     */
    @Valid
    @NotNull
    private HashMap<ImageSizeEnum, Integer> imageSizeMap = new HashMap<ImageSizeEnum, Integer>();


    /**
     * PhotoResizer Value
     */
    @Valid
    @NotNull
    private int largeImageSize;

    /**
     * PhotoResizer Value
     */
    @Valid
    @NotNull
    private int mediumImageSize;

    /**
     * PhotoResizer Value
     */
    @Valid
    @NotNull
    private int thumbImageSize;

    /**
     * getImageSize method for returning the max int value of thumb, medium or large images
     * @param size an ImageSizeEnum set to LARGE, MEDIUM or THUMB
     * @return int
     */
    public int getImageSize(ImageSizeEnum size)
    {
        return imageSizeMap.get(size).intValue();
    }

    /**
     * Getter for largeImageSize
     *
     * @return int
     */
    @JsonProperty
    public int getLargeImageSize()
    {
        return largeImageSize;
    }

    /**
     * Setter for largeImageSize
     * @param largeImageSize the max pixel value for a large image, e.g. 2048. -1 means use the original size
     */
    @JsonProperty
    public void setLargeImageSize(int largeImageSize)
    {
        this.largeImageSize = largeImageSize;
        imageSizeMap.put(ImageSizeEnum.LARGE,this.largeImageSize);
    }

    /**
     * Getter for mediumImageSize
     * @return int
     */
    @JsonProperty
    public int getMediumImageSize()
    {
        return mediumImageSize;
    }

    /**
     * Setter for mediumImageSize
     * @param mediumImageSize the max pixel value for a medium image, e.g. 1280. -1 means use the original size
     */
    @JsonProperty
    public void setMediumImageSize(int mediumImageSize)
    {
        this.mediumImageSize = mediumImageSize;
        imageSizeMap.put(ImageSizeEnum.MEDIUM,this.mediumImageSize);
    }

    /**
     * Getter for thumbImageSize
     *
     * @return int
     */
    @JsonProperty
    public int getThumbImageSize()
    {
        return thumbImageSize;
    }

    /**
     * Setter for thumbImageSize
     * @param thumbImageSize the max pixel value for a thum image, e.g. 128. -1 means use the original size
     */
    @JsonProperty
    public void setThumbImageSize(int thumbImageSize)
    {
        this.thumbImageSize = thumbImageSize;
        imageSizeMap.put(ImageSizeEnum.THUMB,this.thumbImageSize);
    }
}
