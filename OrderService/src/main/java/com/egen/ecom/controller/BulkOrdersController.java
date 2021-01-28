package com.egen.ecom.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import com.egen.ecom.dto.request.BulkOrdersCancelRequest;
import com.egen.ecom.dto.request.BulkOrdersCreateRequest;
import  com.egen.ecom.dto.response.ErrorMessageResponse;
import  com.egen.ecom.dto.response.MessageResponse;
import  com.egen.ecom.service.KafkaService;
import  com.egen.ecom.util.OrderIdGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping(value = "/api/v1/bulkOrders", produces = "application/json", consumes = { "application/json", "*/*" })
public class BulkOrdersController {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	private KafkaService kafkaService;

	@Value(value = "${kafka.orderservice.create.topic}")
	private String orderCreateTopic;

	@Value(value = "${kafka.orderservice.cancel.topic}")
	private String orderCancelTopic;

	@Autowired
	private OrderIdGenerator squenceGenerator;

	@ApiOperation(httpMethod = "POST", value = "Bulk Create Order", response = String.class, responseContainer = "")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Order Item Not Found!"),
			@ApiResponse(code = 400, message = "Order BAD REQUEST FOUND!"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR!") })
	@PostMapping
	public ResponseEntity<?> createBulkOrders(@Valid @RequestBody BulkOrdersCreateRequest ordersReq) {

		LOG.info("Intialized Bulk Order Processing API={}", "/bulkOrders");
		List<Long> orderIds = new ArrayList<>();
		try {
			ordersReq.getOrders().forEach(order -> {
				Long id = squenceGenerator.nextId();
				orderIds.add(id);
				kafkaService.sendMessage(orderCreateTopic, id.toString(), order);
			});
		} catch (Exception e) {
			LOG.info("Bulk Order Processing Error in API={}", "/bulkOrders");
			LOG.debug(e.getStackTrace().toString());
			return new ResponseEntity<>(
					new ErrorMessageResponse(500, "Bulk Order Create failed due to 500 error!", "Unable to Save Bulk Orders", "/orders"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		LOG.info("Bulk Order Processing was successfull API={}", "/bulkOrders");
		return ResponseEntity.ok(orderIds);
	}

	@ApiOperation(httpMethod = "POST", value = "Bulk Cancel Order", response = String.class, responseContainer = "")
	@ApiResponses(value = { @ApiResponse(code = 202, message = "Bulk Order cancel request accepted successfully") })
	@PostMapping("/cancellation")
	public ResponseEntity<?> updateBulkOrders(@Valid @RequestBody BulkOrdersCancelRequest ordersReq) {

		LOG.info("Initiating Bulk Order Processing API={}", "/bulkOrders/cancellation");
		try {
			kafkaService.sendMessage(orderCancelTopic, "/bulkOrders/cancellation", ordersReq.getOrders());
		} catch (Exception e) {
			LOG.info("Bulk Order Processing Error API={}", "bulkOrders/cancellation");
			LOG.debug(e.getStackTrace().toString());
			return new ResponseEntity<>(new ErrorMessageResponse(500, "Bulk Order Update failed!",
					"Unable to Update Bulk Order Status", "/bulkOrders/cancellation"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		LOG.info("Bulk Order Processing was successfull API={}", "/bulkOrders/cancellation");
		return new ResponseEntity<>(new MessageResponse("Bulk Order Cancel request Accepted"), HttpStatus.ACCEPTED);
	}
}
