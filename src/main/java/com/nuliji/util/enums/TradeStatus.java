package com.nuliji.util.enums;

/**
 * Created by gaojie on 2017/5/11.
 */
public class TradeStatus {

    public static final short WAIT_BUYER_PAY = 0;
    public static final short SUCCESS = 1;
    public static final short CANCEL = 2;
    public static final short FINISH = 3;
    public static final short ERROR = 4;
    public static final short CLOSE = 5;
    public static final short SYSTEM = 6;
    public static final short EXCEPTION = 7;

    public static boolean isPayed(int status){
        return status == SUCCESS || status == FINISH;
    }

    public static boolean isPaying(int status){
        return status == WAIT_BUYER_PAY || status == ERROR;
    }

    public static boolean isInvalid(int status){
        return status == CLOSE || status == CANCEL || status == SYSTEM || status == EXCEPTION;
    }

    public static boolean isError(int status){
        return status == ERROR;
    }
}
