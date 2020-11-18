package com.itheima.dianshang.service.impl;

import com.itheima.dianshang.dao.OrderDOMapper;
import com.itheima.dianshang.dao.SequenceDOMapper;
import com.itheima.dianshang.dataobject.OrderDO;
import com.itheima.dianshang.dataobject.SequenceDO;
import com.itheima.dianshang.error.BusinessException;
import com.itheima.dianshang.error.EmBussinessError;
import com.itheima.dianshang.service.ItemService;
import com.itheima.dianshang.service.OrderService;
import com.itheima.dianshang.service.UserService;
import com.itheima.dianshang.service.model.ItemModel;
import com.itheima.dianshang.service.model.OrderModel;
import com.itheima.dianshang.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    UserService userService;

    @Autowired
    ItemService itemService;

    @Autowired
    OrderDOMapper orderDOMapper;

    @Autowired
    SequenceDOMapper sequenceDOMapper;



    //订单流水号的生成
    @Transactional(propagation = Propagation.REQUIRES_NEW) //即使事务失败,将事务进行回滚，订单号还是要生成新的订单号
    public String generatorOrderNo() {
//        System.out.println("===========hhhhha" + "==========" + Math.random() * 1000);
        StringBuilder builder = new StringBuilder();
        int sequence = 0;
        //前8位为时间，年月日
        //jdk8的LocalDateTime
        LocalDateTime time = LocalDateTime.now();
        //2019-12-10 将"-"用""代替
        String nowDate = time.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        builder.append(nowDate);
        //中间6位为自增序列
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        //拿到序列号
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        //凑足6位 不够的用0填充
        for (int i = 0; i < 6 - sequenceStr.length(); i++) {
            builder.append("0");
        }
        builder.append(sequenceStr);
        //后面2位为分库分表位暂时写死
        builder.append("00");
        return builder.toString();
    }

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount) throws BusinessException {
        //一。首先进行入参校验
        //首先判断用户是否存在
        UserModel userModel = userService.getUserById(userId);

        if (userModel == null) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "用户不存在");
        }

        //根据传来的商品id,判断该商品是否存在
        //调用了商品表的业务层实现类
        ItemModel itemModel = itemService.getItemById(itemId);

        if (itemModel == null) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "订单不存在");
        }

        //判断数量是否合理
        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBussinessError.STOCK_OF_ENOUGH);
        }


        //二。落单减库存。下单之后，需要更改有库存字段的那张商品表。
        //这里要注意，是落单减库存，而不是支付减库存。下单之后，立刻减库存
        //需要在有库存字段的那张商品表对应的映射配置文件（就是ItemStockDOMapper.xml）中
        //添加一个减库存的方法（就是decreaseByStock方法），别忘了在对应持久层接口中声明这个方法
        //然后在商品表的业务层实现类中添加这个decreaseByStock方法，然后在此处被调用
        // 别忘了在商品表的业务层接口中声明这个方法
        boolean flage = itemService.decreaseByStock(itemId, amount);
        if(!flage){
            throw new BusinessException(EmBussinessError.STOCK_OF_ENOUGH);
        }

        //订单入库

        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setAmount(amount);
        orderModel.setItemId(itemId);
        orderModel.setItemPrice(itemModel.getPrice());


        //订单的流水号
        orderModel.setId(generatorOrderNo());

        OrderDO orderDO = convertFromOrderModel(orderModel);

        orderDOMapper.insertSelective(orderDO);

        itemService.increaseSales(itemId, amount);

        return orderModel;
    }

    public OrderDO convertFromOrderModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        return orderDO;
    }
}
