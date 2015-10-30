package com.nginx.image.resources;

import com.codahale.metrics.annotation.Timed;
import com.nginx.image.core.PhotoResizer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

/**
 * Created by cstetson on 10/9/15.
 */

@Path("/resize_image")
public class PhotoResizerResource
{

    public final PhotoResizer imageProcessor = new PhotoResizer();;

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
        String imageDataJSON = imageProcessor.resizeImage(url);
        return imageDataJSON;
    }
}
