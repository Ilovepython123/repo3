package com.itheima.dianshang.service.impl;

import com.itheima.dianshang.dao.PromoDOMapper;
import com.itheima.dianshang.dataobject.PromoDO;
import com.itheima.dianshang.service.PromoService;
import com.itheima.dianshang.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    PromoDOMapper promoDOMapper;

    @Override
    public PromoModel getPromoModelByItemId(Integer itemId) {
        PromoDO promoDO = promoDOMapper.getPromoByItemId(itemId);
        PromoModel promoModel = convertFromPromoDo(promoDO);
        //该商品没有参见秒杀活动
        if (promoModel == null) {
            return null;
        }

        //判断当前的活动状态
        DateTime now = new DateTime();

        if (now.isBefore(promoModel.getStartDate())) {
            //还没开始，因为当前时间在开始活动的时间之前
            promoModel.setStatus(1);
        } else if (now.isAfter(promoModel.getEndDate())) {
            //活动结束啦，因为当前时间在结束活动的时间之后
            promoModel.setStatus(3);
        } else {
            //活动正在进行中
            promoModel.setStatus(2);
        }
        return promoModel;
    }



    //将PromoDo转化为PromoModel
    public PromoModel convertFromPromoDo(PromoDO promoDO) {
        if (promoDO == null) {
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        return promoModel;
    }
}
