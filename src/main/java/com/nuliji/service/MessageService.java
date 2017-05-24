package com.nuliji.service;

import com.nuliji.exception.InsideException;
import com.nuliji.util.Response;
import org.springframework.stereotype.Service;

/**
 * 统一返回消息
 * Created by gaojie on 2017/5/11.
 */
@Service
public class MessageService {
    public Response success(Object result){
        return new Response(0, "", result);
    }

    public Response error(int status, String message){
        return new Response(status, message, null);
    }

    public Response exception(InsideException e){
        return new Response(e.getCode(), e.getMessage(), null);
    }
}
