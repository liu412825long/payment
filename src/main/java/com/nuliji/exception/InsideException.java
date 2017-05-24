package com.nuliji.exception;

/**
 * Created by gaojie on 2017/5/11.
 */
abstract public class InsideException extends Throwable {
    protected int code = 0;

    public InsideException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode(){
        return code;
    }
}
