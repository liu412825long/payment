package com.nuliji.exception;

/**
 * Created by gaojie on 2017/5/11.
 */
public class ParameterException extends InsideException {
    public ParameterException(int code, String message){
        super(code, message);
    }
}
