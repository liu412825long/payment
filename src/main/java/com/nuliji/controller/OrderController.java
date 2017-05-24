package com.nuliji.controller;
/**
 * Created by gaojie on 2017/5/10.
 */

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.nuliji.exception.InsideException;
import com.nuliji.pay.common.ResultInfo;
import com.nuliji.proj.Order;
import com.nuliji.proj.Pay;
import com.nuliji.service.MessageService;
import com.nuliji.service.OrderService;
import com.nuliji.service.PayService;
import com.nuliji.service.TradeService;
import com.nuliji.util.data.*;
import com.nuliji.util.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Resource
    private OrderService orderService;
    @Resource
    private TradeService tradeService;
    @Resource
    private MessageService messageService;
    @Resource
    private PayService payService;

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    /**
     * 查询订单列表
     * @param userId
     * @param offset
     * @param number
     * @return
     */
    @RequestMapping(value="/list", method = RequestMethod.GET)
    @ResponseBody
    public Response getList(
            @RequestParam("userId") String userId,
            @RequestParam("offset") int offset,
            @RequestParam("number") int number
    ) {
        List<Order> orderList = orderService.getList(userId, offset, number);

        return messageService.success(orderList);
    }

    /**
     * 购买
     * @param content：json串ConsumeRequestData
     * @return
     */
    @RequestMapping(value="/consume", method = RequestMethod.POST)
    @ResponseBody
    public Response consume(
            @RequestBody String content
    ){
        ConsumeRequestData consumeRequest = JSON.parseObject(content, ConsumeRequestData.class);
        try{
            ConsumeResponseData result = orderService.consume(consumeRequest);
            return messageService.success(result);
        }catch (InsideException e){
            return messageService.exception(e);
        }
    }

    /**
     * 充值
     * @param content：json串PayRequestData
     * @param request
     * @return
     */
    @RequestMapping(value="/pay", method=RequestMethod.POST)
    @ResponseBody
    public Response pay(
            @RequestBody String content,
            HttpServletRequest request
    ){
        PayRequestData payRequest = JSON.parseObject(content, PayRequestData.class);

        try{
            String pid = tradeService.preprocess(payRequest);
            Pay pay = payService.pay(payRequest, pid, getClientIp(request));
            PayResponseData result = tradeService.process(pay);
            // {request:{}, pay_id}
            return messageService.success(result);
        }catch(InsideException e){
            return messageService.exception(e);
        } catch (Exception e){
            logger.error(e.getMessage());
            return messageService.error(500, "系统错误");
        }
    }

    /**
     * 查询充值状态
     * @param payId
     * @return
     */
    @RequestMapping(value="/confirm", method=RequestMethod.GET)
    @ResponseBody
    public Response confirm(
            @RequestParam("payId") String payId
    ){
        try {
            Pay pay = payService.get(payId);
            ResultInfo info = tradeService.query(pay);
            if(info.getId().isEmpty()) return messageService.error(101, "支付不存在");
            PayResultData result = tradeService.client(pay, info);
            return messageService.success(result);
        } catch (InsideException e) {
            return messageService.exception(e);
        } catch (Exception e){
            logger.error(e.getMessage());
            return messageService.error(500, "系统错误");
        }
    }

    private String getClientIp(HttpServletRequest request){
        String ipAddress = request.getHeader("x-forwarded-for");

        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if(ipAddress!=null && ipAddress.length()>15){ //"***.***.***.***".length() = 15
            if(ipAddress.indexOf(",")>0){
                ipAddress = ipAddress.substring(0,ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }
}
