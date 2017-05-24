package com.nuliji.pay;

import com.nuliji.pay.common.PayInfo;
import com.nuliji.pay.common.ResultInfo;
import com.nuliji.util.Configure;
import com.tencent.WXPay;
import com.tencent.common.Signature;
import com.tencent.common.Util;
import com.tencent.protocol.pay_protocol.AppReqData;
import com.tencent.protocol.pay_protocol.UnifiedOrderReqData;
import com.tencent.protocol.pay_protocol.UnifiedOrderResData;
import com.tencent.protocol.pay_query_protocol.ScanPayQueryReqData;
import com.tencent.protocol.pay_query_protocol.ScanPayQueryResData;
import com.tencent.service.ScanPayQueryService;
import com.tencent.service.UnifiedOrderService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Result;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by gaojie on 2017/5/10.
 */
public class Weixin implements PaySource {
    //添加一个日志器
    private static final Logger logger = LoggerFactory.getLogger(Weixin.class);
    private static Weixin instance = null;
    static{
        instance = new Weixin();
    }
    public static Weixin getInstance(){
        return instance;
    }
    public Weixin(){
        WXPay.initSDKConfiguration(
                Configure.weixinAppKey(),
                Configure.weixinAppId(),
                Configure.alipayPid(),
                "","",""
        );
    }

    @Override
    public ResultInfo notifyTrade(HttpServletRequest request) throws Exception {
        InputStream is = request.getInputStream();
        String content= IOUtils.toString(is, "utf-8");
        logger.debug("weixin notify:"+content);
        // 支付完成才会发送异步通知
        ScanPayQueryResData scanPayQueryResData = (ScanPayQueryResData) Util.getObjectFromXML(content, ScanPayQueryResData.class);
        String sign = scanPayQueryResData.getSign();
        scanPayQueryResData.setSign("");
        boolean flag = checkSign(scanPayQueryResData.toMap(), sign);
        ResultInfo info = new ResultInfo();
        info.setResponse(scanPayQueryResData);
        if(!flag){
            info.setStatus("system");
            info.setMessage("签名校验错误");
            return info;
        }
        info = parseResult(scanPayQueryResData, "success");
        info.setMessage("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
        return info;
    }

    @Override
    public ResultInfo returnTrade(ResultInfo info) {
        String response = info.getResponse().toString();
        if(response.indexOf("get_brand_wcpay_request") == 0){
            // wap返回
            switch(response){
                case "get_brand_wcpay_request:ok":
                    info.setStatus("wait");
                    break;
                case "get_brand_wcpay_request:fail":
                    info.setStatus("system");
                    break;
                case "get_brand_wcpay_request:cancel":
                    info.setStatus("system");
                    break;
                default:
                    info.setStatus("system");
            }
        }else{
            // app返回
            switch(response){
                case "0":
                    info.setStatus("wait");
                    break;
                case "-1":
                    info.setStatus("system");
                    break;
                case "-2":
                    info.setStatus("system");
                    break;
                default:
                    info.setStatus("system");
            }
        }
        return info;
    }

    @Override
    public boolean validTrade(ResultInfo info, PayInfo payInfo) {
        if(info.getAppId() != payInfo.getAppId()) return false;
        if(info.getPid() != payInfo.getPid()) return false;

        return true;
    }

    @Override
    public PayInfo appTrade(String payId, String pid, BigDecimal money, String subject, String body, String notifyUrl, String ip) throws Exception{
        UnifiedOrderReqData unifiedOrderReqData = new UnifiedOrderReqData(body, "", payId, (int)(money.floatValue() * 100), "", ip, "", "", "", notifyUrl, "APP");
        //接受API返回
        String payServiceResponseString;
        UnifiedOrderService unifiedOrderService = new UnifiedOrderService();
        payServiceResponseString = unifiedOrderService.request(unifiedOrderReqData);

        //将从API返回的XML数据映射到Java对象
        UnifiedOrderResData unifiedOrderResData = (UnifiedOrderResData) Util.getObjectFromXML(payServiceResponseString, UnifiedOrderResData.class);

        PayInfo payInfo = new PayInfo();
        payInfo.setPid(pid);
        payInfo.setAppId(Configure.alipayAppId());
        payInfo.setMoney(money);
        String prepayid = unifiedOrderResData.getPrepay_id();
        if(!prepayid.isEmpty()){
            AppReqData appReqData = new AppReqData(prepayid);
            payInfo.setRequest(appReqData);
            payInfo.setStatus("ok");
        }else{
            payInfo.setStatus("fail");
            payInfo.setRequest(unifiedOrderResData);
        }

        return payInfo;
    }

    @Override
    public ResultInfo getTrade(String payId, String transactionNo, String pid) throws Exception{
        String payQueryServiceResponseString;

        ScanPayQueryService scanPayQueryService = new ScanPayQueryService();

        ScanPayQueryReqData scanPayQueryReqData = new ScanPayQueryReqData("", payId);
        payQueryServiceResponseString = scanPayQueryService.request(scanPayQueryReqData);

        logger.debug("weixin getTrade:"+payQueryServiceResponseString);

        //将从API返回的XML数据映射到Java对象
        ScanPayQueryResData scanPayQueryResData = (ScanPayQueryResData) Util.getObjectFromXML(payQueryServiceResponseString, ScanPayQueryResData.class);

        ResultInfo info = parseResult(scanPayQueryResData, "ok");
        if(info.getStatus() == "ok"){
            /**
             * SUCCESS—支付成功
             REFUND—转入退款
             NOTPAY—未支付
             CLOSED—已关闭
             REVOKED—已撤销(刷卡支付)
             USERPAYING--用户支付中
             PAYERROR--支付失败(其他原因，如银行返回失败)
             */
            switch(scanPayQueryResData.getTrade_state()){
                case "SUCCESS":
                    info.setStatus("success");
                    break;
                case "REFUND":
                    info.setStatus("refund");
                    break;
                case "NOTPAY":
                    info.setStatus("wait");
                    break;
                case "REVOKED":
                    info.setStatus("cancel");
                    break;
                case "USERPAYING":
                    info.setStatus("wait");
                    break;
                case "PAYERROR":
                    info.setStatus("error");
                    break;
            }
        }
        return info;
    }

    private ResultInfo parseResult(ScanPayQueryResData response, String status){
        ResultInfo info = new ResultInfo();
        info.setAppId(response.getAppid());
        info.setPid(response.getMch_id());
        info.setResponse(response);
        info.setId(response.getOut_trade_no());
        info.setMoney(BigDecimal.valueOf(Float.valueOf(response.getTotal_fee())/100));
        info.setOpen_id(response.getOpenid());
        info.setTransaction_no(response.getTransaction_id());

        if (response == null || response.getReturn_code() == null) {
            return formatInfo(info, "system", "请求错误");
        }

        if (response.getReturn_code().equals("FAIL")) {
            return formatInfo(info, "system", response.getReturn_msg());
        } else {
            if (response.getResult_code().equals("SUCCESS")) {//业务层成功
                return formatInfo(info, status, "");
            } else {
                return formatInfo(info, "error", response.getErr_code_des());
            }
        }
    }

    private ResultInfo formatInfo(ResultInfo info, String status, String message){
        info.setStatus(status);
        info.setMessage(message);
        return info;
    }

    private boolean checkSign(Map param, String sign){
        String signNow = Signature.getSign(param);
        return signNow == sign;
    }
}
