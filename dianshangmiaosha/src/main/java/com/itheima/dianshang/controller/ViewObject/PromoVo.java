package com.itheima.dianshang.controller.ViewObject;

import org.joda.time.DateTime;

import java.math.BigDecimal;

public class PromoVo {

    //秒杀活动商品的显示
    //秒杀时刻的商品价格
    private BigDecimal promoItemPrice;
    //秒杀开始的时间
    private DateTime startDate;
    //秒杀活动的id
    private Integer id;



    //秒杀的状态 1还没开始 2进行中 3以结束
    private Integer status;

    public BigDecimal getPromoItemPrice() {
        return promoItemPrice;
    }

    public void setPromoItemPrice(BigDecimal promoItemPrice) {
        this.promoItemPrice = promoItemPrice;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
