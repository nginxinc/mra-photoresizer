package com.nginx.image.resources;

import com.codahale.metrics.annotation.Timed;
import com.nginx.image.core.PhotoResizer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 //  PhotoresizerResource.java
 //  PhotoResizer
 //
 //  Copyright © 2017 NGINX Inc. All rights reserved.
 */

@Path("/v1/image")
public class PhotoResizerResource {
    public PhotoResizerResource() {}

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getGreeting() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "\t<meta charset=\"utf-8\" />\n" +
                "\t<title>Untitled</title>\n" +
                "\t<meta name=\"generator\" content=\"BBEdit 10.5\" />\n" +
                "</head>\n" +
                "<body style=\"margin:3em;\"><form action=\"/v1/image\" method=\"POST\">\n" +
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

    public String resizeImage(@FormParam("url") String url) {
        PhotoResizer imageProcessor = new PhotoResizer();
        return imageProcessor.resizeImage(url);
    }
}
