package com.nuliji.pay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayOpenPublicTemplateMessageIndustryModifyRequest;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayOpenPublicTemplateMessageIndustryModifyResponse;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.nuliji.pay.common.PayInfo;
import com.nuliji.pay.common.ResultInfo;
import com.nuliji.util.Configure;
import com.tencent.protocol.pay_protocol.AppReqData;
import com.tencent.protocol.pay_query_protocol.ScanPayQueryResData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by gaojie on 2017/5/10.
 */
public class Alipay implements PaySource{
    //添加一个日志器
    private static final Logger logger = LoggerFactory.getLogger(Alipay.class);
    private static Alipay instance = null;
    private AlipayClient alipayClient = null;
    static{
        instance = new Alipay();
    }
    public static Alipay getInstance(){
        return instance;
    }
    public Alipay(){
        alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", Configure.alipayAppId(), Configure.alipayAppKey(), "json", "utf-8", Configure.alipayAppPublicKey(), "RSA2");
    }
    @Override
    public ResultInfo notifyTrade(HttpServletRequest request) throws Exception{
        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        logger.debug("alipay notify:"+params.toString());
//切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
//boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
        boolean flag = AlipaySignature.rsaCheckV1(params, Configure.alipayAppPublicKey(), "utf-8", "RSA2");
        ResultInfo info = new ResultInfo();
        info.setResponse(params);
        if(!flag){
            info.setStatus("system");
            info.setMessage("签名校验错误");
            return info;
        }

        String status = params.get("trade_status");
        if(status == "WAIT_BUYER_PAY"){
            info.setStatus("wait");
        }else if(status == "TRADE_CLOSE"){
            info.setStatus("close");
        }else if(status == "TRADE_SUCCESS"){
            info.setStatus("success");
        }else if(status == "TRADE_FINISHED"){
            info.setStatus("finish");
        }
        info.setId(params.get("out_trade_no"));
        info.setTransaction_no(params.get("trade_no"));
        info.setMoney(BigDecimal.valueOf(Float.valueOf(params.get("total_amount"))));
        info.setOpen_id(params.get("buyer_id"));
        info.setMessage("success");
        return info;
    }

    @Override
    public ResultInfo returnTrade(ResultInfo info) {
        Map<String,String> response = (Map)info.getResponse();
        if(response.get("code").isEmpty()){
            // 手机返回
            // let {out_trade_no, trade_no, app_id, total_amount, seller_id, msg, charset, timestamp, code} = content;
            switch(response.get("code")){
                case "9000":
                    info.setStatus("wait");
                    info.setId(response.get("out_trade_no"));
                    info.setTransaction_no(response.get("trade_no"));
                    info.setMoney(BigDecimal.valueOf(Float.valueOf(response.get("total_amount"))));
                    break;
                case "6004":
                case "8000":
                    info.setStatus("wait");
                    info.setMessage("支付处理中");
                    break;
                case "4000":
                    info.setStatus("error");
                    info.setMessage("支付失败");
                    break;
                case "6001":
                    info.setStatus("system");
                    info.setMessage("用户取消支付");
                    break;
                case "6002":
                    info.setStatus("system");
                    info.setMessage("网络链接错误");
                    break;
                case "5000":
                default:
                    info.setStatus("system");
                    info.setMessage("请联系管理员");
            }
        }else{
            // wap返回
            // let {app_id, method, sign_type, sign, charset, timestamp, version, out_trade_no, trade_no, total_amount, seller_id} = content;
            info.setId(response.get("out_trade_no"));
            info.setTransaction_no(response.get("trade_no"));
            info.setMoney(BigDecimal.valueOf(Float.valueOf(response.get("total_amount"))));
            info.setMessage("success");
            info.setStatus("wait");
        }
        return info;
    }

    @Override
    public boolean validTrade(ResultInfo info, PayInfo payInfo) {
//        if(info.getAppId() != payInfo.getAppId()) return false;
//        if(info.getPid() != payInfo.getPid()) return false;

        return true;
    }

    @Override
    public PayInfo appTrade(String payId, String pid, BigDecimal money, String subject, String body, String notifyUrl, String ip) throws Exception{
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
//SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setBody(body);
        model.setSubject(subject);
        model.setOutTradeNo(payId);
        model.setTimeoutExpress("30m");
        model.setTotalAmount(money.toString());
        model.setProductCode("QUICK_MSECURITY_PAY");
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);

        PayInfo payInfo = new PayInfo();
        payInfo.setPid(pid);
        payInfo.setAppId(Configure.alipayAppId());
        payInfo.setMoney(money);

        try {
            //这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
//            System.out.println(response.getBody());//就是orderString 可以直接给客户端请求，无需再做处理。
            payInfo.setRequest(response.getBody());
            payInfo.setStatus("ok");
        } catch (AlipayApiException e) {
            e.printStackTrace();
            payInfo.setStatus("fail");
            payInfo.setRequest(request);
        }
        return payInfo;
    }

    @Override
    public ResultInfo getTrade(String payId, String transactionNo, String pid) throws Exception {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+payId+"\"," +
                "    \"trade_no\":\""+transactionNo+"\"" +
                "  }");
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        logger.debug("alipay getTrade:"+response.toString());

        ResultInfo info = parseResult(response, "ok");
        String status = info.getStatus();
        if(status == "ok") {
            String trade_status = response.getTradeStatus();
            // 交易状态：WAIT_BUYER_PAY（交易创建，等待买家付款）、TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）、TRADE_SUCCESS（交易支付成功）、TRADE_FINISHED（交易结束，不可退款）
            if(status == "WAIT_BUYER_PAY"){
                info.setStatus("wait");
            }else if(status == "TRADE_CLOSE"){
                info.setStatus("close");
            }else if(status == "TRADE_SUCCESS"){
                info.setStatus("success");
            }else if(status == "TRADE_FINISHED"){
                info.setStatus("finish");
            }
        }
        return null;
    }

    private ResultInfo parseResult(AlipayTradeQueryResponse response, String status){
        ResultInfo info = new ResultInfo();
        info.setResponse(response);
        info.setId(response.getOutTradeNo());
        info.setMoney(BigDecimal.valueOf(Float.valueOf(response.getTotalAmount())));
        info.setOpen_id(response.getOpenId());
        info.setTransaction_no(response.getTradeNo());

        if (response == null || response.getCode() == null) {
            return formatInfo(info, "system", "请求错误");
        }
        switch(response.getCode()){
            case "10000": // 调用接口成功
                return formatInfo(info, status, "");
            case "40004": // 业务处理失败
                switch(response.getSubCode()){
                    case "ACQ.EXIST_FORBIDDEN_WORD":
                        return formatInfo(info, "error", response.getSubMsg());
                    case "ACQ.TRADE_HAS_SUCCESS":
                        return formatInfo(info, "success", "");
                    case "ACQ.TRADE_HAS_CLOSE":
                        return formatInfo(info, "close", "当前订单已关闭，请重新支付");
                    case "ACQ.BUYER_BALANCE_NOT_ENOUGH":
                        return formatInfo(info, "error", "用户余额不足");
                    case "ACQ.BUYER_BANKCARD_BALANCE_NOT_ENOUGH":
                        return formatInfo(info, "error", "用户余额不足");
                    case "ACQ.ERROR_BALANCE_PAYMENT_DISABLE":
                        return formatInfo(info, "error", "余额支付需开启");
                    case "ACQ.BUYER_SELLER_EQUAL":
                        return formatInfo(info, "error", "买卖家不能相同");
                    case "ACQ.TRADE_BUYER_NOT_MATCH":
                        return formatInfo(info, "error", "交易买家不匹配");
                    case "ACQ.BUYER_ENABLE_STATUS_FORBID":
                        return formatInfo(info, "error", "用户状态非法");
                    case "ACQ.PULL_MOBILE_CASHIER_FAIL":
                        return formatInfo(info, "error", "请重新支付");
                    case "ACQ.MOBILE_PAYMENT_SWITCH_OFF":
                        return formatInfo(info, "error", "请开启无线支付功能");
                    case "ACQ.PAYMENT_FAIL":
                        return formatInfo(info, "error", "系统错误，请重新支付");
                    case "ACQ.BUYER_PAYMENT_AMOUNT_DAY_LIMIT_ERROR":
                        return formatInfo(info, "error", "买家付款日限额超限");
                    case "ACQ.BEYOND_PAY_RESTRICTION":
                        return formatInfo(info, "error", "商家收款额度超限");
                    case "ACQ.BEYOND_PER_RECEIPT_RESTRICTION":
                        return formatInfo(info, "error", "商家月收款额度超限");
                    case "ACQ.BUYER_PAYMENT_AMOUNT_MONTH_LIMIT_ERROR":
                        return formatInfo(info, "error", "买家月支付额度超限");
                    case "ACQ.SELLER_BEEN_BLOCKED":
                        return formatInfo(info, "error", "商家账号被冻结");
                    case "ACQ.ERROR_BUYER_CERTIFY_LEVEL_LIMIT":
                        return formatInfo(info, "error", "买家未通过人行认证");
                    case "ACQ.PAYMENT_REQUEST_HAS_RISK":
                        return formatInfo(info, "error", "支付方式存在风险");
                    case "ACQ.NO_PAYMENT_INSTRUMENTS_AVAILABLE":
                        return formatInfo(info, "error", "没有可用的支付工具");
                    case "ACQ.USER_FACE_PAYMENT_SWITCH_OFF":
                        return formatInfo(info, "error", "请开启当面付款开关");
                    case "ACQ.SELLER_BALANCE_NOT_ENOUGH":
                        return formatInfo(info, "error", "商户的支付宝账户中无足够的资金进行撤销");
                    case "ACQ.REASON_TRADE_BEEN_FREEZEN":
                        return formatInfo(info, "exception", "当前交易被冻结，不允许进行撤销");
                    case "ACQ.TRADE_NOT_EXIST"://query
                        return formatInfo(info, "close", "查询的交易不存在，请重新支付");
                    default:
                        return formatInfo(info, "system", response.getSubMsg());
                }
            case "20000":// 服务不可用
                return formatInfo(info, "wait", "等待支付处理");
            case "20001":// 授权权限不足
            case "40001":// 缺少必要参数
            case "40002":// 非法参数
            case "40006":// 权限不足
            default:
                return formatInfo(info, "system", response.getMsg());
        }
    }

    private ResultInfo formatInfo(ResultInfo info, String status, String message){
        info.setStatus(status);
        info.setMessage(message);
        return info;
    }
}
