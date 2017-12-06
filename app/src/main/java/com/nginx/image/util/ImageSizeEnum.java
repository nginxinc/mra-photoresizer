package com.nginx.image.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Created 12/2/17 15:48 with IntelliJ IDEA.
 * User: charlespretzer
 */
public enum ImageSizeEnum {
    LARGE(-1, "large"),
    MEDIUM(640, "medium"),
    THUMB(120, "thumb");

    private final Integer pixelSize;
    private final String size;

    ImageSizeEnum(Integer pixelSize, String size) {
        this.pixelSize = pixelSize;
        this.size = StringUtils.lowerCase(size);
    }

    /**
     * Getter for pixelSize
     *
     * @return java.lang.Integer
     */
    public Integer getPixelSize() {
        return pixelSize;
    }

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
                "pixelSize=" + pixelSize +
                ", size='" + size + '\'' +
                '}';
    }
}
