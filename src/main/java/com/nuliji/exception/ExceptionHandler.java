package com.nuliji.exception;

import com.alibaba.fastjson.JSON;
import com.nuliji.util.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by gaojie on 2017/5/11.
 */
public class ExceptionHandler implements HandlerExceptionResolver {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
                                     Exception ex) {
        logger.error(ex.toString());
        ModelAndView mv = new ModelAndView();
            /*  使用response返回    */
        response.setStatus(HttpStatus.OK.value()); //设置状态码
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); //设置ContentType
        response.setCharacterEncoding("UTF-8"); //避免乱码
        response.setHeader("Cache-Control", "no-cache, must-revalidate");
        try {
            response.getWriter().write(JSON.toJSONString(new Response(500, ex.getMessage(), null)));
        } catch (IOException e) {
            logger.error("与客户端通讯异常:"+ e.getMessage(), e);
        }

        return mv;
    }
}
