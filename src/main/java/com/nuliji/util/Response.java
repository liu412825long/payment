package com.nuliji.util;

/**
 * Created by gaojie on 2017/5/11.
 */
public class Response {
    private int status = 0;
    private String message = "";
    private Object result = null;
    public Response(int status, String message, Object result){
        this.status = status;
        this.message = message;
        this.result = result;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Object getResult() {
        return result;
    }
}
