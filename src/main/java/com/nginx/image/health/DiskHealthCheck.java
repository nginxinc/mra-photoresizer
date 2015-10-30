package com.nginx.image.health;

import com.codahale.metrics.health.HealthCheck;
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
        File fileSystem = (File) this.servletContext.getAttribute("javax.servlet.context.tempdir");;

        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxFreeSpace = fileSystem.getUsableSpace();
        long totalSpace = fileSystem.getTotalSpace();
        long freeSpace = fileSystem.getFreeSpace();

        sb.append("free memory: " + format.format(freeSpace / 1024) + "<br/>");
        sb.append("allocated memory: " + format.format(totalSpace / 1024) + "<br/>");
        sb.append("max memory: " + format.format(maxFreeSpace / 1024) + "<br/>");
        sb.append("total free memory: " + format.format((freeSpace + (maxFreeSpace - totalSpace)) / 1024) + "<br/>");
        if ((freeSpace/maxFreeSpace) < .1) {
            return Result.unhealthy(sb.toString());
        }
        return Result.healthy();
    }
}