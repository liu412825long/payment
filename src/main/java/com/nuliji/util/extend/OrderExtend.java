package com.nuliji.util.extend;

import com.nuliji.dao.OrderMapper;
import com.nuliji.proj.Order;
import com.nuliji.proj.Pay;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 订单数据扩展
 * Created by gaojie on 2017/5/11.
 */
public class OrderExtend {
    /**
     * 订单购买
     * @param orderMapper
     * @param money
     * @param description
     * @return
     */
    public static Order consume(OrderMapper orderMapper, BigDecimal money, String description){
        int timestamp = Math.toIntExact(System.currentTimeMillis() / 1000);
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setMoney(money);
        order.setDescription(description);
        order.setDirection((short) -1); // 购买 -1
        order.setCreateTime(timestamp);
        order.setUpdateTime(timestamp);
        order.setOrderStatus((short) 1);
        order.setSource((short) 0);
        order.setPayChannel("");
        order.setPayId("");
        order.setPayStatus((short) 0);
        orderMapper.insert(order);
        return order;
    }

    /**
     * 充值完成，记录订单
     * @param orderMapper
     * @param pay
     * @return
     */
    public static Order payed(OrderMapper orderMapper, Pay pay){
        int timestamp = Math.toIntExact(System.currentTimeMillis() / 1000);
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setMoney(pay.getMoney());
        order.setDescription(pay.getSubject());
        order.setDirection((short) 1); // 充值 1
        order.setCreateTime(timestamp);
        order.setUpdateTime(timestamp);
        order.setOrderStatus((short) 1);
        order.setSource((short) 0);
        order.setPayChannel(pay.getChannel());
        order.setPayId(pay.getId());
        order.setPayStatus((short) 1);
        orderMapper.insert(order);
        return order;
    }

}
