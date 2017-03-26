package com.wzsuper.JerseyAPI.Server.filter;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;

@PreMatching
public class AuthResponseFilter implements ContainerResponseFilter
{

    public void filter ( ContainerRequestContext requestContext , ContainerResponseContext responseContext ) throws IOException
    {

        // TODO Auto-generated method stub
    }

}
