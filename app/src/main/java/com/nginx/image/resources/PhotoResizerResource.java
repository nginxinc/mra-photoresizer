package com.nginx.image.resources;

import com.codahale.metrics.annotation.Timed;
import com.nginx.image.core.PhotoResizer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * PhotoResizerResource class: Copyright © 2017 NGINX Inc. All rights reserved.
 *
 * Defines the GET and POST methods which act as endpoints for the photo resizing
 * functionality
 *
 * Uses {@link javax.ws.rs.Path} annotation to define the endpoint URI
 */
@Path("/v1/image")
public class PhotoResizerResource {

    /**
     * Empty Constructor
     */
    public PhotoResizerResource() {}

    /**
     * resizeImage() method assigned to the {@link javax.ws.rs.POST} HTTP method
     * on the /v1/image endpoint
     *
     * Processes the URL specified by the url parameter. This parameter is expected
     * to be the URL of an image publicly available on the internet or privately
     * available to the node on which the photoresizer service is running.
     *
     * Annotated with:
     *  {@link javax.ws.rs.POST} to specify the HTTP method which this Java method
     *  handles
     *  {@link com.codahale.metrics.annotation.Timed} to monitor response times
     *  {@link javax.ws.rs.Produces} {@link javax.ws.rs.core.MediaType#APPLICATION_JSON}
     *  to specify the response type as JSON
     *  {@link javax.ws.rs.Consumes} {@link javax.ws.rs.core.MediaType#APPLICATION_FORM_URLENCODED}
     *  to declare the expected input type of the request
     *
     * @param url a String annotated with {@link javax.ws.rs.FormParam} to
     *            identify the name of the parameter in the request form
     *            payload
     * @return a JSON String containing the URLs of the resized images
     */
    @POST
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)

    public String resizeImage(@FormParam("url") String url) {
        PhotoResizer imageProcessor = new PhotoResizer();
        return imageProcessor.resizeImage(url);
    }
}
