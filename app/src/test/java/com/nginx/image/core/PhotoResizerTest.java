package com.nginx.image.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nginx.image.PhotoResizerApplication;
import com.nginx.image.PhotoResizerConfiguration;
import com.nginx.image.net.S3Client;
import com.nginx.image.resources.MockDownload;
import com.nginx.image.util.ImageSizeEnum;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 //  PhotoResizerTest.java
 //  PhotoResizer
 //
 //  Copyright © 2017 NGINX Inc. All rights reserved.
 */

public class PhotoResizerTest  {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoResizerTest.class);
    private static final String IMAGE_FILE =
            ResourceHelpers.resourceFilePath("rotation_file_orientation_1.jpg");
    private static final String MOCK_PATH = "https://placeholder/original.jpg";
    private static final String LIVE_IMAGE_PATH =
            "https://s3.amazonaws.com/<your-bucket-image-location>/original.jpg";
    private static final String URL_PARAM = "url";

    /**
     * This is used by the {@link #testResize()} method to process a live image.
     *
     * See the notes in the method description.
     */
    @ClassRule
    public static final DropwizardAppRule<PhotoResizerConfiguration> RULE =
            new DropwizardAppRule<>(PhotoResizerApplication.class,
                    ResourceHelpers.resourceFilePath("PhotoResizer.yaml"));

    /**
     * This test creates a mock S3Client and  calls the {@link PhotoResizer#resizeImage(String)}
     * method
     *
     * @throws Exception: propagates from {@link PhotoResizer#resizeImage(String)}
     */
    @Test
    public void mockResize() throws Exception {

        File resourceFile = new File(IMAGE_FILE);
        LOGGER.info("----- resourceFile: " + resourceFile.length());

        final S3Client mockS3 = mock(S3Client.class);
        doAnswer((InvocationOnMock invocation) -> {
            Object[] args = invocation.getArguments();
            FileUtils.copyFile(resourceFile, (File)args[1]);
            return new MockDownload();
        }).when(mockS3).download(anyString(), any(File.class));

        when(mockS3.fileUpload(any(), anyString())).thenReturn(Boolean.TRUE);

        PhotoResizer resizer = new PhotoResizer(mockS3);
        String resizeJSONString = resizer.resizeImage(MOCK_PATH);

        assertTrue(StringUtils.isNotBlank(resizeJSONString));

        JsonNode resizeJSON = new ObjectMapper().readTree(resizeJSONString);

        /*
         * Valid response String will look like the following:
         * {
         *  "thumb_url":"https://placeholder/thumb.jpg",
         *  "large_width":"512",
         *  "thumb_width":"120",
         *  "medium_height":"640",
         *  "thumb_height": "120",
         *  "medium_width":"640",
         *  "medium_url":"https://placeholder/,medium.jpg",
         *  "large_url":"https://placeholder/large.jpg",
         *  "large_height":"512"}
         */

        JsonNode imageNode;
        for(ImageSizeEnum imageSize : ImageSizeEnum.values()) {
            // check with the sizes
            imageNode = resizeJSON.get(imageSize.getSizeName() + "_width");
            assertNotNull("There is no element for " + imageSize.getSizeName() + "_width",
                    imageNode);
            assertTrue("The width value is less than 0", imageNode.asInt() > 0);
            imageNode = resizeJSON.get(imageSize.getSizeName() + "_height");
            assertNotNull("There is no element for " + imageSize.getSizeName() + "_height",
                    imageNode);
            assertTrue("The height value is less than 0", imageNode.asInt() > 0);

            // check the url
            imageNode = resizeJSON.get(imageSize.getSizeName() + "_url");
            assertNotNull("There is no element for " + imageSize.getSizeName() + "_url",
                    imageNode);
            assertTrue("The url: " + imageNode.asText() + " does not contain " +
                    imageSize.getSizeName() + ".jpg", StringUtils.contains(imageNode.asText(),
                    imageSize.getSizeName() + ".jpg"));
        }
    }

    /**
     * This integration test is meant to be run against a live S3 bucket.
     *
     * By default this test is disabled. In order to enable it, follow these steps
     * 1) Uncomment the @Test annotation below
     * 2) Update the src/test/resources/PhotoResizer.yaml file with values for:
     *  a) your s3 bucket
     *  b) the S3 URL
     *  c) your AWS access key
     *  d) your AWS secret key
     * 3) Update the LIVE_IMAGE_PATH constant above with the path to a valid image
     *
     * @throws Exception propagates from {@link Client#target(URI)}
     */
//    @Test
    public void testResize() throws Exception {

        Client client = new JerseyClientBuilder().build();
        Form resizeImageForm = new Form();
        resizeImageForm.param(URL_PARAM, LIVE_IMAGE_PATH);
        LOGGER.info("===== setUp complete with: " + resizeImageForm.asMap());

        final Response response = client.target("http://localhost:" + RULE.getLocalPort() + "/v1/image")
                .request()
                .post(Entity.entity(resizeImageForm, MediaType.APPLICATION_FORM_URLENCODED));

        LOGGER.info(String.format("Response Status: %s - %s",
                response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        assertEquals(Response.Status.fromStatusCode(response.getStatus()), Response.Status.OK);

    }


}
