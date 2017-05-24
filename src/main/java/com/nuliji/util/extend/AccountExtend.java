package com.nuliji.util.extend;

import com.nuliji.dao.AccountMapper;
import com.nuliji.proj.Account;
import com.nuliji.proj.Order;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 账户数据扩展
 * Created by gaojie on 2017/5/11.
 */
public class AccountExtend {
    /**
     * 创建账号
     * @param accountMapper
     * @param userId
     * @return
     */
    public static Account make(AccountMapper accountMapper, String userId){
        int timestamp = Math.toIntExact(System.currentTimeMillis() / 1000);
        Account account = new Account();
        System.out.println(UUID.randomUUID().toString());
        account.setId(UUID.randomUUID().toString());
        account.setCreateTime(timestamp);
        account.setUpdateTime(timestamp);
        account.setUserId(userId);
        account.setMoney(BigDecimal.valueOf(0));
        accountMapper.insert(account);
        return account;
    }

    /**
     * 根据订单修改账户金额
     * @param accountMapper
     * @param account
     * @param order
     * @return
     */
    public static int change(AccountMapper accountMapper, Account account, Order order){
        BigDecimal money = account.getMoney();
        if(order.getDirection() > 0){
            // 充值
            money = money.add(order.getMoney());
        }else{
            money = money.subtract(order.getMoney());
        }
        account.setMoney(money);
        return accountMapper.updateByPrimaryKey(account);
    }

}
