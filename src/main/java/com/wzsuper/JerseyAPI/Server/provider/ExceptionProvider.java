package com.wzsuper.JerseyAPI.Server.provider;

import com.wzsuper.JerseyAPI.Beans.ErrorMessage;
import com.wzsuper.JerseyAPI.Server.exception.APIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by wangzhen on 2017/3/7.
 */
@Provider
public class ExceptionProvider  implements ExceptionMapper<Exception> {

    final static Logger logger = LoggerFactory.getLogger(ExceptionProvider.class);

    @Override
    public Response toResponse(Exception e) {
        Response.ResponseBuilder ResponseBuilder = null;
        if (e instanceof APIException){
            APIException exp = (APIException) e;
            ErrorMessage entity = new ErrorMessage(exp.getCode(),exp.getMessage());
            ResponseBuilder = Response.ok(entity, MediaType.APPLICATION_JSON);
        }else if(e instanceof NotFoundException){
            ErrorMessage entity = new ErrorMessage(404, "资源未找到");
            ResponseBuilder = Response.ok(entity, MediaType.APPLICATION_JSON);
        }else {
            logger.error("未知异常", e);
            ErrorMessage entity = new ErrorMessage(-1, "未知异常");
            ResponseBuilder = Response.ok(entity, MediaType.APPLICATION_JSON);
        }
        return ResponseBuilder.build();
    }
}
