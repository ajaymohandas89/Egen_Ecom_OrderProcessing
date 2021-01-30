package com.egen.ecom.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.egen.ecom.dto.request.AddressRequest;
import com.egen.ecom.dto.request.ItemRequest;
import com.egen.ecom.dto.request.OrderCreateRequest;
import com.egen.ecom.dto.response.ItemServiceResponse;
import com.egen.ecom.dto.response.PaymentServiceResponse;
import com.egen.ecom.enums.AddressTypeEnum;
import com.egen.ecom.enums.DeliveryTypeEnum;
import com.egen.ecom.enums.OrderStatusEnum;
import com.egen.ecom.model.Address;
import com.egen.ecom.model.Item;
import com.egen.ecom.model.Order;
import com.egen.ecom.model.Payment;
import com.egen.ecom.repository.AddressRepository;
import com.egen.ecom.repository.OrderRepository;
import com.egen.ecom.util.*;

@Service
@Transactional
public class OrderService {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Value(value = "${http.timeout:20}")
	private long timeout;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private ItemService itemService;

	@Autowired
	private OrderIdGenerator squenceGenerator;

	//Method to save the order
	public Order saveOrder(Long orderID, OrderCreateRequest orderReq, List<PaymentServiceResponse> transactions,
			List<ItemServiceResponse> inventoryItems) {
		LOG.info("Started database operation for create order request");
		Address shipping_addr = getOrCreateAddressMapping(orderReq.getOrder_shipping_address(),
				AddressTypeEnum.SHIPPING_ADDRESS);
		Address billing_addr = getOrCreateAddressMapping(orderReq.getOrder_billing_address(),
				AddressTypeEnum.BILLING_ADDRESS);
		
		LOG.info("Address Saved Successfully");
		Order order = new Order();
		order.setOrderID(orderID == null ? squenceGenerator.nextId() : orderID);
		order.setCustomerId(orderReq.getOrderCustomerId());
		order.setTax_fee(orderReq.getTax_fee());
		order.setOrder_shipping_address(shipping_addr);
		order.setOrder_billing_address(billing_addr);
		order.setOrderStatus(OrderStatusEnum.ACCEPTED);
		order.setDeliveryType(fetchDeliveryMethod(orderReq.getDeliveryType()));
		order = orderRepository.save(order);

		LOG.info("Order Saved Successfully");
		//method to save every transaction
		Set<Payment> pays = new HashSet<>();
		for (PaymentServiceResponse tran : transactions) {
			Payment payment = paymentService.savePayment(tran, order);
			pays.add(payment);
		}
		order.setPayments(pays);

		//method to save every items of inventory
		LOG.info("Payments Saved Successfully");
		List<Item> items = new ArrayList<>();
		for (ItemServiceResponse itemRes : inventoryItems) {
			Item item = itemService.saveItem(itemRes, order);
			items.add(item);
		}
		order.setItems(items);
		LOG.info("Items Saved Successfully");
		return order;
	}

	//Method to map addresses
	public Address getOrCreateAddressMapping(AddressRequest addressReq, AddressTypeEnum type) {
		Address addr = null;
		if (null != addressReq.getId() && !addressReq.getId().toString().isEmpty()) {
			Optional<Address> temp = addressRepository.findById(addressReq.getId());
			if (temp.isPresent() && temp.get().getType().equals(type))
				return addressRepository.findById(addressReq.getId()).get();
		}

		addr = new Address();
		addr.setLineOne(addressReq.getLineOne());
		addr.setLineTwo(addressReq.getLineTwo());
		addr.setCity(addressReq.getCity());
		addr.setState(addressReq.getState());
		addr.setZip(addressReq.getZip());
		addr.setType(type);
		addressRepository.save(addr);
		return addr;
	}

	public List<Order> getAllOrders() {
		return orderRepository.findAll();
	}

	public Optional<Order> getOrderByOrderId(Long orderID) {
		return orderRepository.findByOrderID(orderID);
	}

	public Order cancelOrder(Order order) {
		order.setOrderStatus(OrderStatusEnum.CANCELLED);
		return orderRepository.save(order);
	}
	
	//method to create bulk order processing
	public void createBulkOrder(Long orderID, OrderCreateRequest orderReq) {
		try {
			LOG.info("Future REST calls started");
			List<Future<?>> itemFuture = itemService.initiateInventoryCheck(orderReq);
			List<Future<?>> paymentFuture = paymentService.intiatePayment(orderReq);
			LOG.info("Future REST calls ended");

			List<PaymentServiceResponse> transactions = new ArrayList<>();
			for (Future<?> paymentResponse : paymentFuture) {
				PaymentServiceResponse trans = (PaymentServiceResponse) paymentResponse.get(timeout, TimeUnit.SECONDS);
				trans.setAmount(trans.getAmount() / 100);
				transactions.add(trans);
			}

			List<ItemServiceResponse> items = new ArrayList<>();
			for (Future<?> itemResponse : itemFuture) {
				List<ItemServiceResponse> itemsList = (List<ItemServiceResponse>) itemResponse.get(timeout, TimeUnit.SECONDS);

				if (itemsList == null || itemsList.isEmpty()) {
					LOG.error("Order Create failed reason='{}' for OrderID={}", "Unable to find Item", orderID);
					// Call a Cancel Payment API
					return;
				}
				Optional<ItemRequest> reqitem = orderReq.getItems().stream()
						.filter(item -> item.getItemName().equals(itemsList.get(0).getItemName())).findFirst();
				if (reqitem.get().getItemQuantity() > itemsList.get(0).getItemQuantity()) {
					LOG.error("Order Create failed reason='{}' for OrderID={}", "Requested Item Quantity Unavailable",
							orderID);
					// Call a Cancel Payment API
					return;
				}
				itemsList.get(0).setItemQuantity(reqitem.get().getItemQuantity());
				items.addAll(itemsList);
			}
			LOG.debug("Save Action Initiated.");
			try {
				saveOrder(orderID, orderReq, transactions, items);
			} catch (Exception e) {
				// Call a Cancel Payment API
				LOG.error("Order Create failed reason='{}' for OrderID={}", "Unable to Save Order", orderID);
				LOG.error("Error message={}", e.getMessage());
				e.printStackTrace();
				return;
			}
			LOG.info("Order Create successfull for OrderID={}", orderID);
		} catch (TimeoutException e) {
			// Call a Cancel Payment API
			LOG.error("Order Create failed reason='{}' for OrderID={}",
					"Unable to create order due to timeout from one of the services.", orderID);
			LOG.error("Error message={}", e.getMessage());
		} catch (InterruptedException | ExecutionException e) {
			// Call a Cancel Payment API
			LOG.error("Order Create failed reason='{}' for OrderID={}",
					"Unable to create order due to unspecified IO error.", orderID);
			LOG.error("Error message={}", e.getMessage());
		}
	}

	//method to cancel the order in bulk
	public void cancelBulkOrder(Long orderID) {
		try {
			Optional<Order> order = getOrderByOrderId(orderID);
			if (!order.isPresent()) {
				LOG.error("Order Cancel failed reason='{}' for OrderID={}", "Order Not Found", orderID);
				return;
			}
			if (OrderStatusEnum.CANCELLED.equals(order.get().getOrderStatus())) {
				LOG.error("Order Cancel failed reason='{}' for OrderID={}", "Order cannot be updated once cancelled!",
						orderID);
				return;
			}
			cancelOrder(order.get());
		} catch (Exception e) {
			LOG.debug(e.getStackTrace().toString());
			LOG.error("Order Create failed reason='{}' for OrderID={}", "Unable to Cancel Order", orderID);
		}
	}

	//method to validate delivery method
	public boolean validateDeliveryMethod(String delivery_method) {
		for (DeliveryTypeEnum method : DeliveryTypeEnum.values()) {
			if (method.name().equals(delivery_method)) {
				return true;
			}
		}
		return false;
	}

	//method to fetch delivery method
	public DeliveryTypeEnum fetchDeliveryMethod(String delivery_method) {
		for (DeliveryTypeEnum method : DeliveryTypeEnum.values()) {
			if (method.name().equals(delivery_method)) {
				return method;
			}
		}
		return null;
	}
}
