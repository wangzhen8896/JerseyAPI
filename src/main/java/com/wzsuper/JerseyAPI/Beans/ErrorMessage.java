package com.wzsuper.JerseyAPI.Beans;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by wangzhen on 2017/3/8.
 */
@XmlRootElement
public class ErrorMessage {

    private int code;

    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ErrorMessage(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ErrorMessage() {
    }
}
