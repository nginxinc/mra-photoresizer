package com.nginx.image.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Created 12/2/17 15:48 with IntelliJ IDEA.
 * User: charlespretzer
 */
public enum ImageSizeEnum {

    LARGE("large"),
    MEDIUM("medium"),
    THUMB("thumb");

    private final String size;
//    private final PhotoResizerConfiguration configuration;

    ImageSizeEnum(String size) {
        this.size = StringUtils.lowerCase(size);
    }

    /**
    /**
     * Getter for size
     *
     * @return java.lang.String
     */
    public String getSizeName() {
        return size;
    }

    @Override
    public String toString() {
        return "ImageSizeEnum{" +
                ", size='" + size + '\'' +
                '}';
    }
}
