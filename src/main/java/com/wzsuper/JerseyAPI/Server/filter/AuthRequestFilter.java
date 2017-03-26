package com.wzsuper.JerseyAPI.Server.filter;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@PreMatching
public class AuthRequestFilter implements ContainerRequestFilter
{
    final static Logger Log = LoggerFactory.getLogger(AuthRequestFilter.class);
    public void filter ( ContainerRequestContext requestContext ) throws IOException
    {
    }

}
