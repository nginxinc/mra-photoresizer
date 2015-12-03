package com.nginx.image.core;

import com.google.common.base.Optional;
import com.nginx.image.PhotoResizerConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import javax.ws.rs.client.Client;

/**
 * Created by cstetson on 10/9/15.
 * Copyright (C) 2015 Nginx, Inc.
 */

public class PhotoResizerTest
{

    private static final String TMP_FILE = createTempFile();
    //private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-example.yml");

    //@ClassRule
    //public static final DropwizardAppRule<PhotoResizerConfiguration> RULE = new DropwizardAppRule<>(PhotoResizerConfiguration.class);


    private Client client;

    @Before
    public void setUp() throws Exception
    {


    }

    private static String createTempFile() {
        try {
            return File.createTempFile("test-example", null).getAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    @Test
    public void testResize() throws Exception
    {
        //expects an imageFile and an max size int
        //other version expects imageFile and a height/width
       /* final Optional<String> image = Optional.fromNullable("image=");
        final PhotoResizer photoResizer = client.target("http://localhost:" + RULE.getLocalPort() + "/resize_image")
                .queryParam("image", "")
                .request()
                .get(PhotoResizer.class);
        assertThat(photoResizer.resizeImage()).isEqualTo();
        */

    }

    @Test
    public void testMakeJSON() throws Exception
    {

    }


    @Test
    public void testWriteJPEG() throws Exception
    {//expects fileHandle, bufferedImage, compression value float

    }

    @Test
    public void testTransformImage() throws Exception
    {//uses initial image file internally

    }

}
