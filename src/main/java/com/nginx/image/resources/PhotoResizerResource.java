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

@Path("/resize_image")
public class PhotoResizerResource
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoResizerResource.class);

    public PhotoResizerResource()
    {
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getGreeting() {
        return "POST:?image=<imageURL>.jpg";
    }

    @POST
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
   /* public String resizeImage(@FormParam("image") InputStream image,
                               @FormDataParam("image") FormDataContentDisposition fileDetail,
                               @FormParam("filePath") Optional<String> filePath,
                               @FormParam("domain") Optional<String> domain)*/
    public String resizeImage(@FormParam("image") String url)
    {
        //String imageDataJSON = imageProcessor.resizeImage(image, fileDetail,filePath,domain);
        PhotoResizer imageProcessor = new PhotoResizer();;
        String imageDataJSON = imageProcessor.resizeImage(url);
        return imageDataJSON;
    }
}
