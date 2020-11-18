package com.itheima.dianshang.controller;

import com.itheima.dianshang.dao.CeshiDOMapper;
import com.itheima.dianshang.dataobject.CeshiDO;
import com.itheima.dianshang.error.BusinessException;
import com.itheima.dianshang.error.EmBussinessError;
import com.itheima.dianshang.response.CommonReturnType;
import com.itheima.dianshang.service.OrderService;
import com.itheima.dianshang.service.model.OrderModel;
import com.itheima.dianshang.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

@Controller
@RequestMapping("/order")

public class OrderController extends BaseController {
    @Autowired
    OrderService orderService;

    @Autowired
    HttpServletRequest httpServletRequest;


    @Autowired
    CeshiDOMapper ceshiDOMapper;

    @GetMapping(value = "/ceshi")
    @ResponseBody
    public CommonReturnType Ceshi(@RequestParam("itemId") BigDecimal itemId,
                                         @RequestParam("amount") BigDecimal amount) throws BusinessException {
        CeshiDO ceshiDO=new CeshiDO();
        ceshiDO.setZiduan1(itemId);
        ceshiDO.setZiduan2(amount);
        ceshiDOMapper.insertSelective(ceshiDO);
        return CommonReturnType.create(null);
    }

    @PostMapping(value = "/ceshi2")
    @ResponseBody
    public CommonReturnType Ceshi2(@RequestBody CeshiDO ceshiDO) throws BusinessException {

        ceshiDOMapper.insertSelective(ceshiDO);
        return CommonReturnType.create(null);
    }








        @GetMapping(value = "/createOrder")
    @ResponseBody
    public CommonReturnType createrOrder(@RequestParam("itemId") Integer itemId,
                                         @RequestParam("amount") Integer amount) throws BusinessException {



        //判断该用户是否存在
        Boolean is_login = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");

        if (is_login == null || !is_login.booleanValue()) {
            throw new BusinessException(EmBussinessError.USER_NOT_LOGIN);
        }

        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("USER_LOG");
        OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId,  amount);

        return CommonReturnType.create(null);
    }






}

