package com.nuliji.service;

import com.alibaba.fastjson.JSON;
import com.nuliji.dao.AccountMapper;
import com.nuliji.dao.OrderMapper;
import com.nuliji.dao.PayMapper;
import com.nuliji.exception.BusinessException;
import com.nuliji.exception.ParameterException;
import com.nuliji.pay.Alipay;
import com.nuliji.pay.PaySource;
import com.nuliji.pay.Weixin;
import com.nuliji.pay.common.PayInfo;
import com.nuliji.pay.common.ResultInfo;
import com.nuliji.proj.Account;
import com.nuliji.proj.Order;
import com.nuliji.proj.Pay;
import com.nuliji.util.Configure;
import com.nuliji.util.data.PayRequestData;
import com.nuliji.util.data.PayResponseData;
import com.nuliji.util.data.PayResultData;
import com.nuliji.util.extend.AccountExtend;
import com.nuliji.util.extend.OrderExtend;
import com.nuliji.util.extend.PayExtend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;

/**
 * Created by gaojie on 2017/5/10.
 */
@Service
@Transactional
public class TradeService {
    @Resource
    private PayMapper payMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private AccountMapper accountMapper;

    public interface Source {
        String WEIXIN = "weixin";
        String ALIPAY = "alipay";
    }
    public interface Channel{
        String alipayApp = "alipay_app";
        String weixinApp = "weixin_app";
    }
    public static HashMap<String, String> channelMap = new HashMap<>();
    public static Properties payProperties = new Properties();;
    static{
        channelMap.put(Channel.alipayApp, Source.ALIPAY);
        channelMap.put(Channel.weixinApp, Source.WEIXIN);
    }
    //添加一个日志器
    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);

    /**
     * 异步通知处理
     * @param channel
     * @param request
     * @return
     * @throws ParameterException
     * @throws BusinessException
     */
    public ResultInfo notifyPay(String channel, HttpServletRequest request) throws ParameterException,BusinessException, Exception{
        if(channel == null) throw new ParameterException(104, "第三方支付错误");
        PaySource paySource = getHandler(channel);
        return paySource.notifyTrade(request);
    }

    /**
     * 同步结果处理
     * @param channel
     * @param info
     * @return
     * @throws ParameterException
     * @throws BusinessException
     */
    public ResultInfo returnPay(String channel, ResultInfo info) throws ParameterException,BusinessException {
        if(channel == null) throw new ParameterException(104, "第三方支付错误");
        PaySource paySource = getHandler(channel);
        return paySource.returnTrade(info);
    }

    /**
     * 查询支付结果
     * @param pay
     * @return
     * @throws BusinessException
     * @throws Exception
     */
    public ResultInfo query(Pay pay) throws BusinessException, Exception{
        PaySource paySource = getHandler(pay.getChannel());
        return paySource.getTrade(pay.getId(), pay.getTransactionNo(), pay.getPid());
    }

    /**
     * 返回服务端结果
     * @param pay
     * @param info
     * @return
     * @throws BusinessException
     */
    public String server(Pay pay, ResultInfo info) throws BusinessException{

        PaySource paySource = getHandler(pay.getChannel());
        if(paySource.validTrade(info, PayExtend.getInfo(pay))){
            throw new BusinessException(206, "支付校验错误");
        }
        if(pay.getMoney().equals(info.getMoney())){
            throw new BusinessException(206, "支付金额错误");
        }
        analyse(pay, paySource, info);
        if(PayExtend.isPayed(pay)){
            Order order = OrderExtend.payed(orderMapper, pay);
            Account account = accountMapper.selectUserId(pay.getUserId());
            AccountExtend.change(accountMapper, account, order);
            PayExtend.relation(payMapper, pay, order);
        }
        return info.getMessage();
    }

    /**
     * 返回客户端处理结果
     * @param pay
     * @param info
     * @return
     * @throws BusinessException
     * @throws ParameterException
     */
    public PayResultData client(Pay pay, ResultInfo info) throws BusinessException, ParameterException{

        PaySource paySource = getHandler(pay.getChannel());
        if(paySource.validTrade(info, PayExtend.getInfo(pay))){
            throw new BusinessException(206, "支付校验错误");
        }
        if(pay.getMoney().equals(info.getMoney())){
            throw new BusinessException(206, "支付金额错误");
        }
        analyse(pay, paySource, info);
        if(info.getStatus() == "system") throw new BusinessException(500, info.getMessage());
        if(PayExtend.isPayed(pay)){
            Order order = OrderExtend.payed(orderMapper, pay);
            Account account = accountMapper.selectUserId(pay.getUserId());
            AccountExtend.change(accountMapper, account, order);
            PayExtend.relation(payMapper, pay, order);
            return new PayResultData(pay, order, account);
        }else if(PayExtend.isPaying(pay)){
            throw new BusinessException(210, "支付中");
        }else{
            throw new BusinessException(210, info.getMessage());
        }
    }

    /**
     * 支付预处理，校验支付请求
     * @param payRequest
     * @return
     * @throws ParameterException
     */
    public String preprocess(PayRequestData payRequest) throws ParameterException {
        if(Objects.equals(payRequest.channel, ""))
            throw new ParameterException(103, "支付渠道为空");
        String source = getSource(payRequest.channel);
        if(source.isEmpty())
            throw new ParameterException(104, "支付渠道错误");
        if(source == Source.ALIPAY)
            return Configure.alipayPid();
        if(source == Source.WEIXIN)
            return Configure.weixinPid();

        throw new ParameterException(104, "支付渠道错误");
    }

    /**
     * 发起支付
     * @param pay
     * @return
     * @throws BusinessException
     * @throws Exception
     */
    public PayResponseData process(Pay pay) throws BusinessException, Exception {
        String notifyUrl = getNotifyUrl(pay.getChannel());
        String subject = pay.getSubject();
        String payId = pay.getId();
        String body = pay.getBody();
        BigDecimal money = pay.getMoney();
        String pid = pay.getPid();
        String channel = pay.getChannel();
        String source = getSource(channel);
        PayInfo info = null;
        if(channel == Channel.alipayApp) {
            info = Alipay.getInstance().appTrade(payId, pid, money, subject, body, notifyUrl, pay.getClientIp());
        }else if(channel == Channel.weixinApp) {
            info = Weixin.getInstance().appTrade(payId, pid, money, subject, body, notifyUrl, pay.getClientIp());
        }else{
            throw new BusinessException(204, "支付支付渠道错误方式");
        }

        if(Objects.equals(info.getStatus(), "ok")) {
            pay.setPayInfo(JSON.toJSONString(info));
            PayExtend.start(payMapper, pay, info);
        } else {
            PayExtend.system(payMapper, pay, info);
            throw new BusinessException(205, "支付调用错误");
        }
        PayResponseData payResponseData = new PayResponseData();
        payResponseData.payId = payId;
        payResponseData.request = info.getRequest();
        return payResponseData;
    }

    /**
     * 同步处理支付记录
     * @param pay
     * @param paySource
     * @param info
     */
    private void analyse(Pay pay, PaySource paySource, ResultInfo info) throws BusinessException {
        String status = info.getStatus();
        if(PayExtend.isPayed(pay)){
            if(status == "error" || status == "close" || status == "cancel"){
                throw new BusinessException(301, "支付异常：已支付，第三方未支付");

            }
            return ;
        }else if(PayExtend.isInvalid(pay)){
            if(status == "success" || status == "finish"){
                throw new BusinessException(301, "支付异常：已撤销或者关闭，第三方支付成功");
            }
            return ;
        }
        if(status == "success") {
            // 支付成功
            PayExtend.payed(payMapper, pay, info, false);
        }else if(status == "finish"){
            // 完成，无法撤销或者关闭
            PayExtend.payed(payMapper, pay, info, true);
        }else if(status == "error") {
            // 支付错误，需要关闭支付交易
            PayExtend.error(payMapper, pay, info);
        }else if(status == "cancel"){
            // 对方已撤销
            PayExtend.cancel(payMapper, pay, info);
        }else if(status == "close"){
            // 对方已经关闭
            PayExtend.close(payMapper, pay, info);
        }else if(status == "system"){
            // system 开发接口错误
            PayExtend.system(payMapper, pay, info);
        }else if(status == "exception"){
            // exception 支付流程异常
            PayExtend.exception(payMapper, pay, info);
        }
    }

    /**
     * 获取支付
     * @param channel
     * @return
     */
    private String getSource(String channel){
        return channelMap.get(channel);
    }

    /**
     * 获取渠道异步通知
     * @param channel
     * @return
     */
    private String getNotifyUrl(String channel){
        return Configure.payNotifyUrl() + channel;
    }

    /**
     * 获取支付实例
     * @param channel
     * @return
     * @throws BusinessException
     */
    private PaySource getHandler(String channel) throws BusinessException {
        String source = getSource(channel);
        if(source == Source.ALIPAY) return Alipay.getInstance();
        if(source == Source.WEIXIN) return Weixin.getInstance();
        throw new BusinessException(204, "支付支付渠道错误方式");
    }
}
