package com.nuliji.pay;

import com.nuliji.pay.common.PayInfo;
import com.nuliji.pay.common.ResultInfo;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * Created by gaojie on 2017/5/10.
 */
public interface PaySource {
    public ResultInfo notifyTrade(HttpServletRequest request) throws Exception;
    public ResultInfo returnTrade(ResultInfo info);
    public boolean validTrade(ResultInfo info, PayInfo payInfo);
    public PayInfo appTrade(String payId, String pid, BigDecimal money, String subject, String body, String notifyUrl, String ip) throws Exception;
    public ResultInfo getTrade(String payId, String transactionNo, String pid) throws Exception;
}
