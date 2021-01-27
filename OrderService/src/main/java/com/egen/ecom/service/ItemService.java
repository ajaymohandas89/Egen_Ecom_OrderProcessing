package com.egen.ecom.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.egen.ecom.dto.request.ItemRequest;
import com.egen.ecom.dto.request.OrderCreateRequest;
import com.egen.ecom.dto.response.ErrorMessageResponse;
import com.egen.ecom.dto.response.ItemServiceResponse;
import com.egen.ecom.model.Item;
import com.egen.ecom.model.ItemID;
import com.egen.ecom.model.Order;
import com.egen.ecom.repository.ItemRepository;

@Service
public class ItemService {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Value("${service.inventory.api.inventoryCheck}")
	private String inventoryServiceURI;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private RestTemplateHelper restTemplateHelper;

	public Item saveItem(ItemServiceResponse itemRes, Order order) {
		LOG.info("Initiating Item Save Request");
		Item item = new Item();
		item.setItemID(new ItemID(order.getOrderID(), itemRes.getSkuId()));
		item.setItemName(itemRes.getItemName());
		item.setItemCost(itemRes.getItemCost());
		item.setItemQuantity(itemRes.getItemQuantity());
		item.setItemDescription(itemRes.getItemDescription());
		item.setOrder(order);
		return itemRepository.save(item);
	}

	public List<Future<?>> initiateInventoryCheck(OrderCreateRequest orderReq) {
		LOG.info("initliazing REST inventory check item");
		List<Future<?>> itemFuture = new ArrayList<>();
		for (ItemRequest item : orderReq.getItems()) {
			Future<?> itemResponse = restTemplateHelper.getForEntity(ItemServiceResponse.class, ErrorMessageResponse.class,
					List.class, inventoryServiceURI, null, item.getItemName());
			itemFuture.add(itemResponse);
		}
		LOG.info("REST request for invenotory check Item was raised Successfully");
		return itemFuture;
	}
}
