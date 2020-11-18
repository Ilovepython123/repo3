package com.itheima.dianshang.service.impl;

import com.itheima.dianshang.dao.ItemDOMapper;
import com.itheima.dianshang.dao.ItemStockDOMapper;
import com.itheima.dianshang.dataobject.ItemDO;
import com.itheima.dianshang.dataobject.ItemStockDO;
import com.itheima.dianshang.error.BusinessException;
import com.itheima.dianshang.error.EmBussinessError;
import com.itheima.dianshang.service.ItemService;
import com.itheima.dianshang.service.PromoService;
import com.itheima.dianshang.service.model.ItemModel;
import com.itheima.dianshang.service.model.PromoModel;
import com.itheima.dianshang.validator.ValidationResult;
import com.itheima.dianshang.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {


    @Autowired
    ItemDOMapper itemDOMapper;
    @Autowired
    ItemStockDOMapper itemStockDOMapper;
    @Autowired
    ValidatorImpl validatorImpl;

    @Autowired
    PromoService promoService;



    @Transactional
    public void increaseSales(Integer itemId, Integer amount) {
        itemDOMapper.increaseBySales(itemId, amount);
    }
    //下订单减库存

    @Transactional
    public boolean decreaseByStock(Integer itemId, Integer amount) {
        int result = itemStockDOMapper.decreaseStock(itemId, amount);
        if (result > 0) {
            return true;
        } else {
            return false;
        }
    }

    public List<ItemModel> listItem() {
        List<ItemDO> itemList = itemDOMapper.selectItemList();
        //stream中的map()实现类型的转化，运用了jdk1.8的stream流操作以及lambda表达式
        List<ItemModel> modelList = itemList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = convetItemModeFromItemStockDO(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());

        System.out.println("====" + modelList + "====");
        return modelList;
    }

    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        ValidationResult validationResult = validatorImpl.validate(itemModel);
        if (validationResult.isHasErroes()) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, validationResult.getErrMsg());
        }
        //数据写到数据库中
        ItemDO itemDO = convertItemDOFromItemModel(itemModel);
        itemDOMapper.insertSelective(itemDO);
        //这里一定要把item中的id设置进来,不让item_stock中的item_id不能关联
        itemModel.setId(itemDO.getId());
        ItemStockDO itemStockDO = convertItemStockDoFromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);
        //返回创建完成后数据库里的对象

        return this.getItemById(itemDO.getId());
    }


    @Transactional
    public ItemModel getItemById(Integer id) {
        ItemModel itemModel = new ItemModel();
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(id);
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        itemModel = convetItemModeFromItemStockDO(itemDO, itemStockDO);

        //判断当前商品是否在参见秒杀活动
        PromoModel promoModel = promoService.getPromoModelByItemId(itemDO.getId());


        //秒杀的活动还没有结束
        if (promoModel != null && promoModel.getStatus().intValue() != 3) {
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }



    //将ItemModel转化为ItemDO
    public ItemDO convertItemDOFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel, itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }
    //将ItemModel转化为ItemStockDO
    public ItemStockDO convertItemStockDoFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setStock(itemModel.getStock());
        itemStockDO.setItemId(itemModel.getId());
        return itemStockDO;
    }
    //将ItemDO和ItemStaockDO转化为ItemModel
    public ItemModel convetItemModeFromItemStockDO(ItemDO itemDO, ItemStockDO itemStockDO) {
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO, itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }
}
