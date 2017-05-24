package com.nuliji.util.extend;

import com.alibaba.fastjson.JSON;
import com.nuliji.dao.PayMapper;
import com.nuliji.pay.common.PayInfo;
import com.nuliji.pay.common.ResultInfo;
import com.nuliji.proj.Order;
import com.nuliji.proj.Pay;
import com.nuliji.util.enums.TradeStatus;
import com.nuliji.util.i.PayModule;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 充值操作扩展
 * Created by gaojie on 2017/5/11.
 */
public class PayExtend {
    /**
     * 发起充值
     * @param payMapper
     * @param pay
     * @param info
     * @return
     */
    public static int start(PayMapper payMapper, Pay pay, PayInfo info){
        pay.setPayInfo(JSON.toJSONString(info));
        pay.setTradeStatus(TradeStatus.WAIT_BUYER_PAY);
        return payMapper.updateByPrimaryKey(pay);
    }

    /**
     * 系统支付错误
     * @param payMapper
     * @param pay
     * @param info
     * @return
     */
    public static int system(PayMapper payMapper, Pay pay, PayInfo info){
        pay.setPayInfo(JSON.toJSONString(info));
        pay.setTradeStatus(TradeStatus.SYSTEM);
        return payMapper.updateByPrimaryKey(pay);
    }

    /**
     * 系统结果错误
     * @param payMapper
     * @param pay
     * @param info
     * @return
     */
    public static int system(PayMapper payMapper, Pay pay, ResultInfo info){
        pay.setResultInfo(JSON.toJSONString(info));
        pay.setTradeStatus(TradeStatus.SYSTEM);
        return payMapper.updateByPrimaryKey(pay);
    }

    /**
     * 支付流程异常
     * @param payMapper
     * @param pay
     * @param info
     * @return
     */
    public static int exception(PayMapper payMapper, Pay pay, ResultInfo info){
        pay.setPayInfo(JSON.toJSONString(info));
        pay.setTradeStatus(TradeStatus.EXCEPTION);
        return payMapper.updateByPrimaryKey(pay);
    }

    /**
     * 支付关闭
     * @param payMapper
     * @param pay
     * @param info
     * @return
     */
    public static int close(PayMapper payMapper, Pay pay, ResultInfo info){
        pay.setPayInfo(JSON.toJSONString(info));
        pay.setTransactionNo(info.getTransaction_no());
        pay.setTradeStatus(TradeStatus.CLOSE);
        return payMapper.updateByPrimaryKey(pay);
    }

    /**
     * 支付错误，需要重新支付
     * @param payMapper
     * @param pay
     * @param info
     * @return
     */
    public static int error(PayMapper payMapper, Pay pay, ResultInfo info){
        pay.setPayInfo(JSON.toJSONString(info));
        pay.setTransactionNo(info.getTransaction_no());
        pay.setTradeStatus(TradeStatus.ERROR);
        return payMapper.updateByPrimaryKey(pay);
    }

    /**
     * 支付取消
     * @param payMapper
     * @param pay
     * @param info
     * @return
     */
    public static int cancel(PayMapper payMapper, Pay pay, ResultInfo info){
        pay.setPayInfo(JSON.toJSONString(info));
        pay.setTransactionNo(info.getTransaction_no());
        pay.setTradeStatus(TradeStatus.CANCEL);
        return payMapper.updateByPrimaryKey(pay);
    }

    /**
     * 支付完成
     * @param payMapper
     * @param pay
     * @param info
     * @param finish
     * @return
     */
    public static int payed(PayMapper payMapper, Pay pay, ResultInfo info, boolean finish){
        int timestamp = Math.toIntExact(System.currentTimeMillis() / 1000);
        pay.setTransactionNo(info.getTransaction_no());
        pay.setResultInfo(JSON.toJSONString(info));
        pay.setPayTime(timestamp);
        pay.setTradeStatus(finish ? TradeStatus.FINISH : TradeStatus.SUCCESS);
        return payMapper.updateByPrimaryKey(pay);
    }

    /**
     * 已支付
     * @param pay
     * @return
     */
    public static boolean isPayed(Pay pay){
        return TradeStatus.isPayed(pay.getTradeStatus());
    }

    /**
     * 支付中
     * @param pay
     * @return
     */
    public static boolean isPaying(Pay pay){
        return TradeStatus.isPaying(pay.getTradeStatus());
    }

    /**
     * 无效支付
     * @param pay
     * @return
     */
    public static boolean isInvalid(Pay pay){
        return TradeStatus.isInvalid(pay.getTradeStatus());
    }

    /**
     * 支付错误
     * @param pay
     * @return
     */
    public static boolean isError(Pay pay){
        return TradeStatus.isError(pay.getTradeStatus());
    }

    /**
     * 获取支付信息
     * @param pay
     * @return
     */
    public static PayInfo getInfo(Pay pay){
        return (PayInfo) JSON.parse(pay.getPayInfo());
    }

    /**
     * 创建支付记录
     * @param payMapper
     * @param userId
     * @param channel
     * @param money
     * @param module
     * @param pid
     * @param subject
     * @param body
     * @param clientIp
     * @return
     */
    public static Pay make(PayMapper payMapper, String userId, String channel, BigDecimal money, String module, String pid, String subject, String body, String clientIp){
        int timestamp = Math.toIntExact(System.currentTimeMillis() / 1000);
        Pay pay = new Pay();
        pay.setId(UUID.randomUUID().toString());
        pay.setUserId(userId);
        pay.setChannel(channel);
        pay.setCreateTime(timestamp);
        pay.setUpdateTime(timestamp);
        pay.setCurrency("CNY");
        pay.setModule(module);
        pay.setModuleId("");  // 支付成功后创建order
        pay.setPayTime(0);
        pay.setPid(pid);
        pay.setSubject(subject);
        pay.setMoney(money);
        pay.setBody(body);
        pay.setTradeStatus((short) 0);
        pay.setTransactionNo(""); // 发起支付后得到
        pay.setClientIp(clientIp);
        pay.setResultInfo("");  // 支付返回后得到
        pay.setPayInfo(""); // 发起支付的请求参数
        payMapper.insert(pay);
        return pay;
    }

    /**
     * 关联模块
     * @param payMapper
     * @param pay
     * @param order
     * @return
     */
    public static int relation(PayMapper payMapper, Pay pay, PayModule order){
        pay.setModuleId(order.getModuleId());
        return payMapper.updateByPrimaryKey(pay);
    }
}
