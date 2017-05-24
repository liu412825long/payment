package com.nuliji.controller;

/**
 * Created by gaojie on 2017/5/10.
 */

import com.alibaba.fastjson.JSON;
import com.nuliji.exception.InsideException;
import com.nuliji.pay.common.ResultInfo;
import com.nuliji.proj.Pay;
import com.nuliji.service.MessageService;
import com.nuliji.service.PayService;
import com.nuliji.service.TradeService;
import com.nuliji.util.Response;
import com.nuliji.util.data.PayResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/pay")
public class PayController {

    @Resource
    private TradeService tradeService;
    @Resource
    private PayService payService;

    @Resource
    private MessageService messageService;

    //添加一个日志器
    private static final Logger logger = LoggerFactory.getLogger(PayController.class);

    /**
     * 微信app的异步通知，需要开启访问，域名路径在pay.properties中定义
     * @param request
     * @return
     */
    @RequestMapping("/weixin_app")
    @ResponseBody
    public String weixinAppNotify(HttpServletRequest request)
    {
        try {
            ResultInfo info = tradeService.notifyPay(TradeService.Channel.weixinApp, request);
            if (info.getId().isEmpty()) return "支付签名错误";
            Pay pay = payService.get(info.getId());
            if (pay == null) return "支付信息不存在";
            return tradeService.server(pay, info);
        }catch (InsideException e){
            return "error";
        }catch (Exception e){
            return "error";
        }
    }

    /**
     * 支付宝aoo的异步通知，需要开启访问，域名路径在pay.properties中定义
     * @param request
     * @return
     */
    @RequestMapping("/alipay_app")
    @ResponseBody
    public String alipayAppNotify(HttpServletRequest request)
    {
        try {
            ResultInfo info = tradeService.notifyPay(TradeService.Channel.alipayApp, request);
            if (info.getId().isEmpty()) return "支付签名错误";
            Pay pay = payService.get(info.getId());
            if (pay == null) return "支付信息不存在";
            return tradeService.server(pay, info);
        }catch (InsideException e){
            return "error";
        }catch (Exception e){
            return "error";
        }
    }

    @RequestMapping("/weixin_app_return")
    @ResponseBody
    public Response weixinAppReturn(@RequestBody String content){

        ResultInfo payRequest = JSON.parseObject(content, ResultInfo.class);
        try {
            ResultInfo info = tradeService.returnPay(TradeService.Channel.weixinApp, payRequest);
            if (info.getId().isEmpty()) return messageService.error(101, "支付签名错误");
            Pay pay = payService.get(info.getId());
            if (pay == null) return messageService.error(101, "支付信息不存在");
            PayResultData result = tradeService.client(pay, info);
            return messageService.success(result);
        }catch (InsideException e){
            return messageService.exception(e);
        }
    }

    @RequestMapping("/alipay_app_return")
    @ResponseBody
    public Response alipayAppReturn(@RequestBody String content){
        ResultInfo payRequest = JSON.parseObject(content, ResultInfo.class);

        try {
            ResultInfo info = tradeService.returnPay(TradeService.Channel.alipayApp, payRequest);
            if (info.getId().isEmpty()) return messageService.error(101, "支付签名错误");
            Pay pay = payService.get(info.getId());
            if (pay == null) return messageService.error(101, "支付信息不存在");
            PayResultData result = tradeService.client(pay, info);
            return messageService.success(result);
        }catch (InsideException e){
            return messageService.exception(e);
        }
    }
}
