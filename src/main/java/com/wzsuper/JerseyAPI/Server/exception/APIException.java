package com.wzsuper.JerseyAPI.Server.exception;

/**
 * Created by wangzhen on 2017/3/8.
 */
public class APIException extends RuntimeException{

    private int code;

    private String message;

    public APIException(){

    }

    public APIException(int code, String message){
        this.code = code;
        this.message = message;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
