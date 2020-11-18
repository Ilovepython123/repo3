package com.itheima.dianshang.service;

import com.itheima.dianshang.service.model.PromoModel;

public interface PromoService {
    //通过itemid查询秒杀的商品

    public PromoModel getPromoModelByItemId(Integer itemId);
}
