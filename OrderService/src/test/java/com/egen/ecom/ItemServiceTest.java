package com.egen.ecom;

import com.egen.ecom.dto.response.ItemServiceResponse;
import com.egen.ecom.model.Item;
import com.egen.ecom.model.Order;
import com.egen.ecom.repository.ItemRepository;
import com.egen.ecom.service.ItemService;
import com.egen.ecom.service.RestTemplateHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ItemServiceTest {

    @InjectMocks
    ItemService itemService;

    @Mock
    ItemRepository itemRepository;

    @Mock
    RestTemplateHelper restTemplateHelper;

    private String inventoryServiceURI = "https://my-json-server.typicode.com/ajaymohandas89/Egen_Ecom_OrderProcessing/items?itemName={itemName}";

    @Test
    public void saveItemTest(){
        ItemServiceResponse itemServiceResponse = CommonUtil.mockItemServiceResponse();
        Order order = CommonUtil.mockOrder();
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(CommonUtil.mockItem());
        Item item = itemService.saveItem(itemServiceResponse,order);
        Assert.assertNotNull(item.getItemCost());
        Assert.assertNotNull(item);
        Assert.assertNotNull(item.getItemName());
    }
}
