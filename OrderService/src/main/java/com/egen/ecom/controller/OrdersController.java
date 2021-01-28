package com.egen.ecom.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.validation.Valid;

import com.egen.ecom.dto.request.OrderCreateRequest;
import com.egen.ecom.dto.request.ItemRequest;
import com.egen.ecom.dto.response.ErrorMessageResponse;
import com.egen.ecom.dto.response.ItemServiceResponse;
import com.egen.ecom.dto.response.OrderResponse;
import com.egen.ecom.dto.response.OrdersListResponse;
import com.egen.ecom.dto.response.PaymentServiceResponse;
import com.egen.ecom.enums.OrderStatusEnum;
import com.egen.ecom.model.Order;
import com.egen.ecom.service.ItemService;
import com.egen.ecom.service.OrderService;
import com.egen.ecom.service.PaymentService;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "OrderResource")
@RequestMapping(value = "/api/v1/orders", produces = "application/json", consumes = { "application/json", "*/*" })
public class OrdersController {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	private OrderService orderService;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private ItemService itemService;

	@Autowired
	private ModelMapper modelMapper;

	@Value(value = "${http.timeout:20}")
	private long timeout;

	@ApiOperation(httpMethod = "POST", value = "Create Order", response = OrderResponse.class, responseContainer = "")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Order Item Not Found!"),
			@ApiResponse(code = 400, message = "Order Bad Input Data!"),
			@ApiResponse(code = 500, message = "Order Create failed!") })
	@PostMapping
	public ResponseEntity<?> createOrder(@Valid @RequestBody OrderCreateRequest orderReq) {
		// Get order id, check inventory, do payment, get customer, set transaction
		// Call API
		try {
			if (!orderService.validateDeliveryMethod(orderReq.getDeliveryType())) {
				LOG.info("Data Validation Error={} for API={}", "Delivery Option Not Supported by the application", "/Orders");
				return new ResponseEntity<>(new ErrorMessageResponse(400, "Order Create failed!",
						"Delivery Option Not Supported by the application", "/orders"), HttpStatus.BAD_REQUEST);
			}

			LOG.info("Initiating  Order Processing API={}", "/orders");
			List<Future<?>> itemFuture = itemService.initiateInventoryCheck(orderReq);
			List<Future<?>> paymentFuture = paymentService.intiatePayment(orderReq);
			LOG.info("End Future REST calls ended");

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
					// Call Cancel Payment API
					LOG.info("Inventory Check Error={} for API={}", "Unable to find Item", "/orders");
					return new ResponseEntity<>(
							new ErrorMessageResponse(404, "Order Create failed since item was not found!", "Unable to find Item", "/orders"),
							HttpStatus.NOT_FOUND);
				}
				Optional<ItemRequest> reqitem = orderReq.getItems().stream()
						.filter(item -> item.getItemName().equals(itemsList.get(0).getItemName())).findFirst();
				if (reqitem.get().getItemQuantity() > itemsList.get(0).getItemQuantity()) {
					LOG.info("Inventory Check Error={} for API={}", "Requested Item Quantity Unavailable in the system", "/orders");
					return new ResponseEntity<>(new ErrorMessageResponse(422, "Order Create failed!",
							"Requested Item Quantity Unavailable in the repository", "/orders"), HttpStatus.UNPROCESSABLE_ENTITY);
				}
				itemsList.get(0).setItemQuantity(reqitem.get().getItemQuantity());
				items.addAll(itemsList);
			}

			Order order = null;
			try {
				order = orderService.saveOrder(null, orderReq, transactions, items);
			} catch (Exception e) {
				LOG.error("Order Create failed with message={} for API={}", e.getMessage(), "/orders");
				return new ResponseEntity<>(
						new ErrorMessageResponse(500, "Order Create failed!", "Unable to Save Order", "/orders"),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			LOG.info("Order Create Successfully");
			return ResponseEntity.ok(modelMapper.map(order, OrderResponse.class));
		} catch (TimeoutException e) {
			LOG.error("Order Processing Error API={}", "/orders");
			e.printStackTrace();
			return new ResponseEntity<>(
					new ErrorMessageResponse(500, "Order Create failed!",
							"Unable to create order due to timeout from one of the services. with 500 error", "/orders"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error(" Order Processing Error API={}", "/orders");
			e.printStackTrace();
			return new ResponseEntity<>(
					new ErrorMessageResponse(500, "Order Create failed!",
							"Unable to create order due to unspecified IO error.", "/orders"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(httpMethod = "GET", value = "Get All Orders", response = OrdersListResponse.class, responseContainer = "")
	@ApiResponses(value = { @ApiResponse(code = 500, message = "Orders Fetch All Failed with 500 error") })
	@GetMapping
	public ResponseEntity<?> fetchAllOrders() {
		LOG.info("Initiating Order Processing API={}", "/orders");
		List<Order> orderList = null;
		OrdersListResponse orders = new OrdersListResponse();
		try {
			orderList = orderService.getAllOrders();
			orderList.forEach(order -> {
				orders.getOrders().add(modelMapper.map(order, OrderResponse.class));
			});
		} catch (Exception e) {
			LOG.error(" Order Processing Error API={}", "/orders");
			e.printStackTrace();
			return new ResponseEntity<>(new ErrorMessageResponse(500, "", "Orders Fetch All Failed!", "/orders"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		LOG.info("Orders Fetch Successfully done");
		return ResponseEntity.ok(orders);
	}

	@ApiOperation(httpMethod = "GET", value = "Get Order By ID", response = OrderResponse.class, responseContainer = "")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Order Not Found!"),
			@ApiResponse(code = 500, message = "Order Fetch By ID Failed due to 500 error!") })
	@GetMapping("/{orderID}")
	public ResponseEntity<?> fetchOrderByID(@PathVariable Long orderID) {
		LOG.info("Initiating Order Processing API={}", "/orders/{orderID}");
		Optional<Order> order = null;
		try {
			order = orderService.getOrderByOrderId(orderID);
			if (!order.isPresent()) {
				return new ResponseEntity<>(new ErrorMessageResponse(404, "", "Order Not Found!", "/orders/{orderID}"),
						HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOG.error(" Order Processing Error API={}", "/orders/{orderID}");
			e.printStackTrace();
			return new ResponseEntity<>(
					new ErrorMessageResponse(500, "", "Order Fetch By ID Failed!", "/orders/{orderID}"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		LOG.info("Order Fetch ID Successfully done");
		return ResponseEntity.ok(modelMapper.map(order.get(), OrderResponse.class));
	}

	@ApiOperation(httpMethod = "POST", value = "Cancel Order By ID", response = OrderResponse.class, responseContainer = "")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Order Not Found!"),
			@ApiResponse(code = 422, message = "Order Cannot Be Updated Once Cancelled!"),
			@ApiResponse(code = 500, message = "Order Fetch By ID Failed!") })
	@PostMapping("/{orderID}/cancellation")
	public ResponseEntity<?> cancelOrderByID(@PathVariable Long orderID) {
		LOG.info("Initiating Order Processing API={}", "/orders/{orderID}/cancellation");
		Optional<Order> order = null;
		Order cancelledOrder = null;
		try {
			order = orderService.getOrderByOrderId(orderID);
			if (!order.isPresent()) {
				LOG.info(" Order Processing Error={} API={}", "Order cannot be updated once cancelled!",
						"/orders/{orderID}/cancellation");
				return new ResponseEntity<>(
						new ErrorMessageResponse(404, "", "Order Not Found!", "/orders/{orderID}/cancellation"),
						HttpStatus.NOT_FOUND);
			}
			if (OrderStatusEnum.CANCELLED.equals(order.get().getOrderStatus())) {
				LOG.info(" Order Processing Error={} API={}", "Order cannot be updated once cancelled!",
						"/orders/{orderID}/cancellation");
				return new ResponseEntity<>(new ErrorMessageResponse(422, "", "Order cannot be updated once cancelled!",
						"/orders/{orderID}/cancellation"), HttpStatus.UNPROCESSABLE_ENTITY);
			}
			cancelledOrder = orderService.cancelOrder(order.get());
		} catch (Exception e) {
			LOG.error(" Order Processing Error API={}", "/orders/{orderID}/cancellation");
			e.printStackTrace();
			return new ResponseEntity<>(
					new ErrorMessageResponse(500, "", "Order Cancel By ID failed due to 500 erro!", "/orders/{orderID}/cancellation"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		LOG.info("Order Cancel Successfully completed");
		return ResponseEntity.ok(modelMapper.map(cancelledOrder, OrderResponse.class));
	}
}
