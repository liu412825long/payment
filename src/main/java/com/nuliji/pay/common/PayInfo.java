package com.nuliji.pay.common;

import java.math.BigDecimal;

/**
 * 统一支付信息
 * Created by gaojie on 2017/5/11.
 */
public class PayInfo {

    private String appId = "";
    private String pid = "";
    private Object request = null;
    private String transaction_no = "";
    private BigDecimal money = null;
    private String status = "";

    public PayInfo(){

    }

    public PayInfo(String status, String appId, String pid, Object request, String id, String transaction_no, BigDecimal money){
        this.status = status;
        this.appId = appId;
        this.pid = pid;
        this.request = request;
        this.transaction_no = transaction_no;
        this.money = money;
    }


    public Object getRequest() {
        return request;
    }

    public void setRequest(Object request) {
        this.request = request;
    }

    public String getTransaction_no() {
        return transaction_no;
    }

    public void setTransaction_no(String transaction_no) {
        this.transaction_no = transaction_no;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}