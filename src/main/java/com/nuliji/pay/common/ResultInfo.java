package com.nuliji.pay.common;

import java.math.BigDecimal;

/**
 * 统一结果信息
 * Created by gaojie on 2017/5/11.
 */
public class ResultInfo {

    private String appId = "";
    private String pid = "";
    private String status = "";
    private String message = "";
    private Object response = null;
    private Object request = null;
    private String id = "";
    private String transaction_no = "";
    private BigDecimal money = null;
    private String open_id = "";

    public ResultInfo(){

    }

    public ResultInfo(String status, String message, Object response, Object request, String id, String transaction_no, BigDecimal money, String open_id){
        this.status = status;
        this.message = message;
        this.response = response;
        this.request = request;
        this.id = id;
        this.transaction_no = transaction_no;
        this.money = money;
        this.open_id = open_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public Object getRequest() {
        return request;
    }

    public void setRequest(Object request) {
        this.request = request;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getOpen_id() {
        return open_id;
    }

    public void setOpen_id(String open_id) {
        this.open_id = open_id;
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
}