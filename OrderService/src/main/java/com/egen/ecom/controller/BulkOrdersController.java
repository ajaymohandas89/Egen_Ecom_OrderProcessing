package com.egen.ecom.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    
}
