package com.nuliji.service;

import com.nuliji.dao.AccountMapper;
import com.nuliji.dao.OrderMapper;
import com.nuliji.dao.PayMapper;
import com.nuliji.exception.BusinessException;
import com.nuliji.exception.ParameterException;
import com.nuliji.proj.Account;
import com.nuliji.proj.Pay;
import com.nuliji.util.i.PayModule;
import com.nuliji.util.data.PayRequestData;
import com.nuliji.util.extend.AccountExtend;
import com.nuliji.util.extend.PayExtend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Objects;

/**
 * 充值服务
 * Created by gaojie on 2017/5/11.
 */
@Service
@Transactional
public class PayService {
//    @Resource(name="sqlSessionFactory")
//    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private PayMapper payMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private AccountMapper accountMapper;

    private static final Logger logger = LoggerFactory.getLogger(PayService.class);

    /**
     * 获取充值记录
     * @param id
     * @return pay
     * @throws ParameterException
     */
    public Pay get(String id) throws ParameterException{
        Pay pay = payMapper.selectByPrimaryKey(id);
        if(pay==null){
            throw new ParameterException(101, "支付不存在");
        }
        return pay;
    }

    /**
     * 获取充值对应的业务模块
     * @param pay
     * @return payModule
     * @throws BusinessException
     */
    public PayModule getInstance(Pay pay) throws BusinessException{
        String module = pay.getModule();
        if(Objects.equals(module, "order")){
            return (PayModule) orderMapper.selectByPrimaryKey(pay.getModuleId());
        }else{
            throw new BusinessException(202, "支付模块错误");
        }
    }

    /**
     * 充值操作
     * @param payRequest
     * @param pid
     * @param clientIp
     * @return pay
     * @throws BusinessException
     * @throws ParameterException
     */
    public Pay pay(PayRequestData payRequest, String pid, String clientIp) throws BusinessException, ParameterException{
        String userId = payRequest.userId;
        Account account = accountMapper.selectUserId(userId);
        int timestamp = Math.toIntExact(System.currentTimeMillis() / 1000);
        if(account == null){
            account = AccountExtend.make(accountMapper, userId);
        }
        Pay pay = PayExtend.make(payMapper, userId, payRequest.channel, payRequest.money, "order", pid, payRequest.subject, payRequest.body, clientIp);
        return pay;
    }
}
