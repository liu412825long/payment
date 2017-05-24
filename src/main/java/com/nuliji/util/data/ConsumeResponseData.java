package com.nuliji.util.data;

import com.nuliji.proj.Account;
import com.nuliji.proj.Order;

import java.math.BigDecimal;

/**
 * Created by gaojie on 2017/5/11.
 */
public class ConsumeResponseData {
    public String id = "";
    public String userId = "";
    public BigDecimal money = BigDecimal.valueOf(0);
    public BigDecimal amount = BigDecimal.valueOf(0);
    public String description = "";
    public int createTime = 0;

    public ConsumeResponseData(Order order, Account account){
        this.amount = account.getMoney();
        this.money = order.getMoney();
        this.id = order.getId();
        this.userId = order.getUserId();
        this.description = order.getDescription();
        this.createTime = order.getCreateTime();
    }
}
