package com.nuliji.exception;

/**
 * Created by gaojie on 2017/5/11.
 */
public class BusinessException extends InsideException {
    public BusinessException(int code, String message){
        super(code, message);
    }
}
