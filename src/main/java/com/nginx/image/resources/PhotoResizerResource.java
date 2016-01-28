package com.nginx.image.resources;

import com.codahale.metrics.annotation.Timed;
import com.nginx.image.core.PhotoResizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

/**
 * Created by cstetson on 10/9/15.
 * Copyright (C) 2015 Nginx, Inc.
 */

@Path("/v1/image")
public class PhotoResizerResource
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoResizerResource.class);

    public PhotoResizerResource()
    {
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getGreeting()
    {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "\t<meta charset=\"utf-8\" />\n" +
                "\t<title>Untitled</title>\n" +
                "\t<meta name=\"generator\" content=\"BBEdit 10.5\" />\n" +
                "</head>\n" +
                "<body><form action=\"/v1/image\" method=\"POST\">\n" +
                "Test An S3 Image URL: <input type=\"text\" name=\"url\" size=\"100\" value=\"https://s3-us-west-1.amazonaws.com/ngra-images/tests/photoresizer/12345/original.jpg\">\n" +
                "<br><button name=\"submit\" type=\"submit\">\n" +
                "Submit\n" +
                "</button>" +
                "</form>\n" +
                "</body>\n" +
                "</html>\n";
    }

    @POST
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
   /* public String resizeImage(@FormParam("url") InputStream image,
                               @FormDataParam("url") FormDataContentDisposition fileDetail,
                               @FormParam("filePath") Optional<String> filePath,
                               @FormParam("domain") Optional<String> domain)*/
    public String resizeImage(@FormParam("url") String url)
    {
        //String imageDataJSON = imageProcessor.resizeImage(image, fileDetail,filePath,domain);
        PhotoResizer imageProcessor = new PhotoResizer();
        String imageDataJSON = imageProcessor.resizeImage(url);
        return imageDataJSON;
    }
}
