package com.itheima.dianshang.service;

import com.itheima.dianshang.error.BusinessException;
import com.itheima.dianshang.service.model.ItemModel;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ItemService {

    public ItemModel createItem(ItemModel itemModel) throws BusinessException;

    public ItemModel getItemById(Integer id);

    public List<ItemModel> listItem();

    public boolean decreaseByStock(Integer itemId, Integer amount);

    public void increaseSales(Integer itemId, Integer amount);
}
