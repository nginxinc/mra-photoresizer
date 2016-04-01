package com.nginx.image.health;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.io.Files;
import io.dropwizard.jetty.MutableServletContextHandler;

import java.io.File;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.attribute.FileStoreAttributeView;
import java.text.NumberFormat;

/**
 * Created by cstetson on 10/9/15.
 */
public class DiskHealthCheck extends HealthCheck
{
    private final MutableServletContextHandler servletContext = new MutableServletContextHandler();

    public DiskHealthCheck() {}

    @Override
    protected Result check() throws Exception {
        File fileSystem = (File) Files.createTempDir();;

        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxFreeSpace = fileSystem.getUsableSpace();
        long totalSpace = fileSystem.getTotalSpace();
        long freeSpace = fileSystem.getFreeSpace();

        sb.append("free disk space\": \"" + format.format(freeSpace / 1024) + "\",");
        sb.append("\"total disk space\": \"" + format.format(totalSpace / 1024) + "\",");
        sb.append("\"usable disk space\": \"" + format.format(maxFreeSpace / 1024) + "\",");
        sb.append("\"usable percentage of file system\": \"" + (((float) maxFreeSpace/totalSpace) * 100) + "%\"");
        if (((float) maxFreeSpace/totalSpace) < .05)
        {
            return Result.unhealthy(sb.toString());
        }
        return Result.healthy();
    }
}