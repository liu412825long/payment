package com.nuliji.service;

import com.nuliji.dao.AccountMapper;
import com.nuliji.dao.OrderMapper;
import com.nuliji.exception.BusinessException;
import com.nuliji.exception.ParameterException;
import com.nuliji.proj.Account;
import com.nuliji.proj.Order;
import com.nuliji.util.data.ConsumeRequestData;
import com.nuliji.util.data.ConsumeResponseData;
import com.nuliji.util.extend.AccountExtend;
import com.nuliji.util.extend.OrderExtend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单服务
 * Created by gaojie on 2017/5/11.
 */
@Service
@Transactional
public class OrderService {
//    @Resource(name="sqlSessionFactory")
//    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private AccountMapper accountMapper;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    /**
     * 获取订单列表
     * @param userId
     * @param offset
     * @param number
     * @return list
     */
    public List<Order> getList(String userId, int offset, int number) {
        List<Order> orderList = orderMapper.selectList(userId, offset, number);
        logger.debug(orderList.toString());
        return orderList;
    }

    /**
     * 购买接口
     * @param consumeRequest 消费请求结构
     * @return order
     * @throws BusinessException
     * @throws ParameterException
     */
    public ConsumeResponseData consume(ConsumeRequestData consumeRequest) throws BusinessException,ParameterException {
        String userId = consumeRequest.userId;
        BigDecimal money = consumeRequest.money;

        Account account = accountMapper.selectUserId(userId);
        if(account == null){
            throw new ParameterException(102, "账户为空");
        }
        if(account.getMoney().compareTo(money)== -1){
            throw new BusinessException(202, "账户金额不足");
        }
        Order order = OrderExtend.consume(orderMapper, money, consumeRequest.description);
        AccountExtend.change(accountMapper, account, order);
        return new ConsumeResponseData(order, account);
    }
}
