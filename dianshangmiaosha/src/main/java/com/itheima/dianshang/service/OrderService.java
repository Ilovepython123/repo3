package com.itheima.dianshang.service;

import com.itheima.dianshang.error.BusinessException;
import com.itheima.dianshang.service.model.OrderModel;

public interface OrderService {

    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount) throws BusinessException;
}
