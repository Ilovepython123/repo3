package com.itheima.dianshang.controller;


import com.itheima.dianshang.controller.BaseController;
import com.itheima.dianshang.controller.ViewObject.ItemVo;
import com.itheima.dianshang.controller.ViewObject.PromoVo;
import com.itheima.dianshang.error.BusinessException;
import com.itheima.dianshang.error.EmBussinessError;
import com.itheima.dianshang.response.CommonReturnType;
import com.itheima.dianshang.service.ItemService;
import com.itheima.dianshang.service.model.ItemModel;
import com.itheima.dianshang.service.model.PromoModel;
import com.sun.org.glassfish.gmbal.ParameterNames;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/item")
public class ItemController extends BaseController {
    @Autowired
    ItemService itemService;

    //新增商品
    @PostMapping(value = "/create")
    @ResponseBody
    public CommonReturnType createItem(@RequestBody ItemModel itemModel) throws BusinessException {

        if (itemModel == null) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }

        ItemModel item = itemService.createItem(itemModel);

        ItemVo itemVo = convertItemVoFromItemModel(item);

        return CommonReturnType.create(itemVo);
    }


    //根据item中的id显示商品信息
    @GetMapping(value = "/getItem")
    @ResponseBody
    public CommonReturnType getItemById(@RequestParam(name = "id") Integer id) {
        ItemModel itemModel = itemService.getItemById(id);
        ItemVo itemVo = convertItemVoFromItemModel(itemModel);
        return CommonReturnType.create(itemVo);
    }

    //显示商品列表
    @GetMapping(value = "/getItemList")
    @ResponseBody
    public CommonReturnType getItemList() {
        List<ItemModel> itemModels = itemService.listItem();

        //运用了jdk1.8的stream流操作以及lambda表达式，最后把流放还成了集合
        List<ItemVo> itemVoList = itemModels.stream().map(itemModel -> {
            ItemVo itemVo = convertItemVoFromItemModel(itemModel);
            return itemVo;
        }).collect(Collectors.toList());

        return CommonReturnType.create(itemVoList);
    }




    //将ItemModel转化为ItemVo
    public ItemVo convertItemVoFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemVo itemVo = new ItemVo();
        BeanUtils.copyProperties(itemModel, itemVo);

        if (itemModel.getPromoModel() != null) {
            itemVo.setPromoVo(this.convertPromoVoFromPromoModel(itemModel.getPromoModel()));
        }

        return itemVo;
    }



    //将PromoModel转化为PromoVo
    public PromoVo convertPromoVoFromPromoModel(PromoModel promoModel) {
        if (promoModel == null) {
            return null;
        }
        PromoVo promoVo = new PromoVo();
        BeanUtils.copyProperties(promoModel, promoVo);

        return promoVo;
    }
}


